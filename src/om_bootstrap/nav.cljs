(ns om-bootstrap.nav
  (:require [clojure.string :as st]
            [om-bootstrap.mixins :as m]
            [om-bootstrap.types :as t]
            [om-bootstrap.util :as u]
            [schema.core :as s]
            [rum])
  (:require-macros [schema.macros :as sm]))

;; ## NavItem

(def NavItem
  (t/bootstrap
   {(s/optional-key :title) s/Str
    (s/optional-key :on-select) (sm/=> s/Any s/Any)
    (s/optional-key :active?) s/Bool
    (s/optional-key :disabled?) s/Bool
    (s/optional-key :href) s/Str}))

(rum/defc nav-item*
  "Generates a nav item for use inside of a nav element."
  [{:keys [opts children]}]
  (let [[bs props] (t/separate NavItem opts {:href "#"})
        classes {:active (:active? bs)
                 :disabled (:disabled? bs)}
        handle-click (fn [e]
                       (when-let [f (:on-select bs)]
                         (.preventDefault e)
                         (when-not (:disabled? bs)
                           (f (:key props)
                              (:href bs)))))]
    [:li (u/merge-props props {:class (u/class-set classes)})
     [:a {:href (:href bs)
          :ref "anchor"
          :title (:title bs)
          :on-click handle-click}
      children]]))

(sm/defn nav-item :- t/Component
  [opts :- NavItem & children]
  (nav-item* {:opts opts
              :children children}))

;; ## Nav

(def Nav
  (t/bootstrap
   {:bs-style (s/enum "tabs" "pills")
    (s/optional-key :active-key) (s/either s/Str s/Num)
    (s/optional-key :active-href) s/Str
    (s/optional-key :stacked?) s/Bool
    (s/optional-key :justified?) s/Bool
    (s/optional-key :collapsible?) s/Bool
    (s/optional-key :expanded?) s/Bool
    (s/optional-key :navbar?) s/Bool
    (s/optional-key :pull-right?) s/Bool}))

(sm/defn child-active? :- s/Bool
  "Accepts a NavItem's child props and the current options provided to
  the Nav bar; returns true if the child component should be active,
  false otherwise."
  [child-props opts]
  (boolean
   (or (:active? child-props)
       (when-let [ak (:active-key opts)]
         (= ak (:key child-props)))
       (when-let [ak (:active-href opts)]
         (= ak (:href child-props))))))

(sm/defn clone-nav-item
  "Takes the options supplied to the top level nav and returns a
  function that will CLONE the inner nav items, transferring all
  relevant props from the outer code to the inner code."
  [opts]
  (letfn [(prop-fn [props]
            (let [base (-> (select-keys opts [:on-select :active-key :active-href])
                           (assoc :active? (child-active? (:opts props) opts)
                                  :nav-item? true))]
              (update-in props [:opts] u/merge-props base)))]
    (fn [child]
      (u/clone-with-props child prop-fn))))

(rum/defc nav*
  [{:keys [opts children]}]
  (let [[bs props] (t/separate Nav opts {:expanded? true
                                         :bs-class "nav"})
        classes {:navbar-collapse (:collapsible? bs)
                 :collapse (not (:expanded? bs))
                 :in (:expanded? bs)}
        ul-props {:ref "ul"
                  :class (u/class-set
                          (merge (t/bs-class-set bs)
                                 {:nav-stacked (:stacked? bs)
                                  :nav-justified (:justified? bs)
                                  :navbar-nav (:navbar? bs)
                                  :pull-right (:pull-right? bs)}))}
        children (map (clone-nav-item opts) children)]
    (if (and (:navbar? bs)
             (not (:collapsible? bs)))
      [:ul (u/merge-props props ul-props) children]
      [:nav (u/merge-props props {:class (u/class-set classes)})
       [:ul ul-props children]])))

(sm/defn nav :- t/Component
  [opts :- Nav & children]
  (nav* {:opts opts
         :children children}))

;; ## SubNav


;; ## NavBar

(def NavBar
  (t/bootstrap
   {(s/optional-key :component-fn) (sm/=> s/Any s/Any)
    (s/optional-key :fixed-top?) s/Bool
    (s/optional-key :fixed-bottom?) s/Bool
    (s/optional-key :static-top?) s/Bool
    (s/optional-key :inverse?) s/Bool
    (s/optional-key :role) s/Str
    (s/optional-key :brand) t/Renderable
    (s/optional-key :on-toggle) (sm/=> s/Any s/Any)
    (s/optional-key :toggle-nav-key) s/Str
    (s/optional-key :nav-expanded?) s/Bool
    (s/optional-key :default-nav-expanded?) s/Bool}))

(defn render-toggle-button [state bs]
  (let [handle-toggle (fn []
                        (when-let [f (:on-toggle bs)]
                          (reset! (:changing? state) true)
                          (f)
                          (reset! (:changing? state) false))
                        (swap! (:nav-open? state) not))
        tb (u/clone-with-props (:toggle-button bs)
                               {:class "navbar-toggle"
                                :on-click handle-toggle})]
    [:button {:class "navbar-toggle"
              :type "button"
              :on-click handle-toggle}
     (or tb [[:span {:class "sr-only" :key 0} "Toggle navigation"]
             [:span {:class "icon-bar" :key 1}]
             [:span {:class "icon-bar" :key 2}]
             [:span {:class "icon-bar" :key 3}]])]))

(sm/defn render-header-and-toggle-btn? :- s/Bool
  "Returns true if any of the necessary properties are in place to
  render the navbar-header and toggle button."
  [bs]
  (boolean
   (or (:brand bs)
       (:toggle-button bs)
       (:toggle-nav-key bs))))

(defn render-header [state bs]
  [:div {:class "navbar-header"}
   (if (u/strict-valid-component? (:brand bs))
     (u/clone-with-props (:brand bs) {:class "navbar-brand"})
     [:span {:class "navbar-brand"} (:brand bs)])
   (when (render-header-and-toggle-btn? bs)
     (render-toggle-button state bs))])

(defn render-navbar-child [state child bs]
  (let [f (fn [props]
            (let [opts (:opts props)
                  collapsible? (or (:collapsible? opts)
                                   (when (:toggle-nav-key bs)
                                     (= (:key opts) (:toggle-nav-key bs))))
                  base {:navbar? true
                        :collapsible? collapsible?
                        :expanded? (and collapsible?
                                        (or (:nav-expanded? bs)
                                            @(:nav-open? state)))}]
              (update-in props [:opts] u/merge-props base)))]
    (u/clone-with-props child f)))

(rum/defcs navbar*
  < (rum/local false :changing?)
  {:should-update
   (fn [old-state new-state]
     (not @(:changing? new-state)))}
  (rum/local false :nav-open?)
  (m/default-local (comp #(get-in % [:opts :default-nav-expanded?]) first :rum/args) :nav-open?)
  [state {:keys [opts children]}]
  (let [children nil ;; this is wrong, causes a hang currently
        [bs props] (t/separate NavBar opts
                               {:bs-class "navbar"
                                :bs-style "default"
                                :role "navigation"
                                :component-fn (fn [opts & c]
                                                [:nav opts c])})
        classes (assoc (t/bs-class-set bs)
                       :navbar-fixed-top (:fixed-top? bs)
                       :navbar-fixed-bottom (:fixed-bottom? bs)
                       :navbar-static-top (:static-top? bs)
                       :navbar-inverse (:inverse? bs))]
    ((:component-fn bs) (u/merge-props (merge bs props)
                                       {:class (u/class-set classes)})
     [:div {:class (if (:fluid props) "container-fluid" "container")}
      (when (render-header-and-toggle-btn? bs)
        (render-header state bs))
      (map #(render-navbar-child state % bs) children)])))

(sm/defn navbar
  [opts :- NavBar & children]
  (navbar* {:opts opts
            :children children}))
