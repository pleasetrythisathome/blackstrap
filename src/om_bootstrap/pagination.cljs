(ns om-bootstrap.pagination
  (:refer-clojure :exclude [next])
  (:require [om-bootstrap.types :as t]
            [om-bootstrap.util :as u]
            [schema.core :as s :include-macros true]))

(def Page
  (t/bootstrap
    {(s/optional-key :disabled?) s/Bool
     (s/optional-key :active?) s/Bool
     (s/optional-key :href) s/Str
     (s/optional-key :on-click) (s/=> s/Any s/Any)}))

(s/defn page :- t/Component [opts :- Page & children]
  (let [[bs props] (t/separate Page opts {:href "#"})
        classes {:disabled (:disabled? bs)
                 :active (:active? bs)}]
    [:li (u/merge-props props {:class (u/class-set classes)})
          [:a {:href (:href bs)
                :on-click (:on-click bs)}
               children]]))

(s/defn previous :- t/Component [opts :- Page]
  (page (assoc opts :aria-label "Previous") [:span {:aria-hidden "true"} "«"]))

(s/defn next :- t/Component [opts :- Page]
  (page (assoc opts :aria-label "Next") [:span {:aria-hidden "true"} "»"]))

(s/defn pagination :- t/Component [opts & children]
  [:nav
    [:ul {:class "pagination"}
          children]])
