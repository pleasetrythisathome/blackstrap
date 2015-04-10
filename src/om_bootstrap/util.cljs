(ns om-bootstrap.util
  "Utilities for the om-bootstrap library."
  (:require [clojure.string :as str]
            [goog.object :as gobject]
            [schema.core :as s :include-macros true]))

(defn merge-with-fns
  "Returns a map that consists of the rest of the maps conj-ed onto
  the first.  If a key occurs in more than one map, the mapping(s)
  from the latter (left-to-right) will be combined with the mapping in
  the result by looking up the proper merge function and in the
  supplied map of key -> merge-fn and using that for the big merge. If
  a key doesn't have a merge function, the right value wins (as with
  merge)."
  [k->fn maps]
  (letfn [(merge-entry [m e]
            (let [k (key e) v (val e)]
              (if-let [f (and (contains? m k)
                              (k->fn k))]
                (assoc m k (f (get m k) v))
                (assoc m k v))))
          (merge [m1 m2]
            (reduce merge-entry (or m1 {}) (seq m2)))]
    (reduce merge {} maps)))

(s/defn collectify :- [s/Any]
  [x :- s/Any]
  (if (sequential? x) x [x]))

;; ## Reactish Utilities
;;
;; Some of these are rewritten from various React addons.

(defn get-props
  "This is the same as om.core/get-props. We added it to get around
  the new precondition in Om 0.8.0."
  [x]
  (aget (.-props x) "__om_cursor"))

(s/defn om-component? :- s/Bool
  [x]
  (boolean (get-props x)))

(s/defn get-state [x]
  (aget (.-props x) ":rum/state"))

(s/defn rum-component? :- s/Bool
  [x]
  (boolean (get-state x)))

(s/defn strict-valid-component? :- s/Bool
  "TODO: Once Om updates its externs to include this file, we can
  remove the janky aget call."
  [child]
  ((aget js/React "isValidElement") child))

(s/defn valid-component? :- s/Bool
  "Returns true if the supplied argument is a valid React component,
  false otherwise."
  [child]
  (or (string? child)
      (number? child)
      (strict-valid-component? child)))

(s/defn some-valid-component? :- s/Bool
  "Returns true if the supplied sequence contains some valid React component,
  false otherwise."
  [children]
  (boolean (some valid-component? children)))

;; TODO: We want to generate a map-valid-component, we have to hook
;; into the implementation of the internal one that can handle numbers
;; and strings properly.

(defn chain-fns
  "Generates a new function that calls each supplied side-effecting
  function."
  [l r]
  (if (and l r)
    (fn [& args]
      (apply l args)
      (apply r args))
    (or l r)))

(def react-merges
  "Map of React keyword to a custom function for its merge. Tries to
  do a decent job with event handlers as well; currently only
  handles :on-select :on-click, :on-blur, kebab-cased as om-tools
  prefers."
  (let [merge-class (fn [l r] (str l " " r))
        orig-fn  (fn [l r] (or l r))
        empty-fn (fn [_ _] nil)]
    {:className merge-class
     :class merge-class
     :style merge
     :children empty-fn
     :key empty-fn
     :ref orig-fn
     :on-select chain-fns
     :on-click chain-fns
     :on-blur chain-fns}))

(defn merge-props
  "Merges two maps that represent React properties. Merges occur
  according to the functions defined in `react-merges`."
  [& prop-maps]
  (letfn [(react-merge [xs]
            (merge-with-fns react-merges xs))
          (normalize-class [m]
            (if (contains? m :class)
              (react-merge [(dissoc m :class) {:className (:class m)}])
              m))]
    (let [ret (react-merge (map normalize-class prop-maps))]
      (if-not (:key ret)
        (dissoc ret :key)
        ret))))

;; ## clone-with-props and helpers

(defn copy-js
  "Returns a basic, shallow copy of the supplied JS object."
  [arr]
  (let [ret (js-obj)]
    (doseq [k (js-keys arr)]
      (when (.hasOwnProperty arr k)
        (aset ret k (aget arr k))))
    ret))

(defn create-element
  ([child] (create-element child nil))
  ([child props]
   (.createElement js/React (.-type child) props)))

(defn update-first-arg
  [state f & args]
  (update state :rum/args (fn [[a & r]]
                            (cons (apply f a args) r))))

(defn clone-rum
  [child extra-props]
  (let [rum-state (js->clj (get-state child))
        props #js {}
        cloned-child (gobject/clone child)]
    (gobject/extend props
      (.-props child)
      #js {":rum/state" (if (fn? extra-props)
                          (update-first-arg rum-state extra-props)
                          (update-first-arg rum-state merge-props extra-props))})
    (gobject/extend cloned-child #js {:props props})
    cloned-child))

(defn clone-om
  "Merges the supplied extra properties into the underlying Om cursor
  and calls the constructor to clone the React component.

  Requires that the supplied child has an Om cursor attached to it! "
  [child extra-props]
  (let [om-props (get-props child)
        props #js {}
        cloned-child (gobject/clone child)]
    (gobject/extend props
      (.-props child)
      #js {:__om_cursor (if (fn? extra-props)
                          (extra-props om-props)
                          (merge-props om-props extra-props))})
    (gobject/extend cloned-child #js {:props props})
    cloned-child))

(defn clone-basic-react
  "This function is called if the React component child was NOT
  generated by Om. Merges the supplied properties into the -props
  field of the supplied React component and creates a shallow copy."
  [child extra-props]
  (let [props (js->clj (.-props child) :keywordize-keys true)
        new-props (merge (if (fn? extra-props)
                           (extra-props props)
                           (merge-props props extra-props))
                         (when-let [children (:children props)]
                           {:children children}))]
    (create-element child (clj->js new-props))))

(defn clone-with-props
  "Returns a shallow copy of the supplied component (child); the copy
  will have any props provided by extra-props merged in. Props are
  merged in the same manner as merge-props, so props like :class will
  be merged intelligently.

  extra-props can be a function of the old props that returns new
  props, OR it can be a map of props.

  If the supplied child is an Om component, any supplied extra
  properties will be merged into the underlying cursor and accessible
  in the Om constructor."
  ([child]
   (clone-with-props child {}))
  ([child extra-props]
   (cond (not (strict-valid-component? child)) child
         (rum-component? child) (clone-rum child extra-props)
         (om-component? child) (clone-om child extra-props)
         (and (map? extra-props)
              (empty? extra-props)) (create-element child (.-props child))
              :else (clone-basic-react child extra-props))))

(defn class-set [m]
  "Returns a string of keys with truthy values joined together by spaces,
   or returns nil when no truthy values."
  (when-let [ks (->> m (filter val) keys (clojure.core/map name) distinct seq)]
    (str/join " " ks)))
