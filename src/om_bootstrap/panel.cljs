(ns om-bootstrap.panel
  (:require [om-bootstrap.mixins :as m]
            [om-bootstrap.types :as t]
            [om-bootstrap.util :as u]
            [schema.core :as s :include-macros true]
            [rum]))

;; TODO: Dropdown functionality is NOT there yet, so :on-select is
;; ignored (https://github.com/racehub/om-bootstrap/issues/17)

(def Panel
  (t/bootstrap
   {(s/optional-key :on-select) (s/=> s/Any s/Any)
    (s/optional-key :header) t/Renderable
    (s/optional-key :footer) t/Renderable
    (s/optional-key :list-group) t/Renderable
    (s/optional-key :collapsible?) s/Bool
    (s/optional-key :collapsed?) s/Bool}))

(declare collapsible-panel*)

(s/defn panel :- t/Component
  [opts :- Panel & children]
  (let [[bs props] (t/separate Panel opts {:bs-class "panel"
                                           :bs-style "default"})
        classes (assoc (t/bs-class-set bs) :panel true)]
    (if (:collapsible? bs)
      (collapsible-panel* {:opts     (dissoc opts :collapsible?)
                            :children children})
      [:div (u/merge-props props {:class (u/class-set classes)})
             (when-let [header (:header bs)]
               [:div {:class "panel-heading"}
                      (u/clone-with-props header {:class "panel-title"})])
             (when-not (= 0 (count (filter identity children)))
               [:div {:class (str "panel-body" (when (:collapsed? bs) " collapse"))
                       :ref   "body"}
                      children])
             (when-let [list-group (:list-group bs)]
               list-group)
             (when-let [footer (:footer bs)]
               [:div {:class "panel-footer"} footer])])))

;; ## Collapsible Panel

(rum/defcs collapsible-panel*
  "Generates a collapsible panel component resposible for its own toggled state.
   The :collapsed? state is handled through a collapsible mixin."
  < (rum/local {:collapsed? false})
  [state {:keys [opts children]}]
  (let [is-collapsed? (:collapsed? @(:rum/local state))
        toggle! (fn [_] (swap! (:rum/local state) update :collapsed? not) false)
        collapsible-header [:h4
                            [:a {:href     "#"
                                 :on-click toggle!}
                             (:header opts)]]]
    (panel (u/merge-props opts {:header collapsible-header
                                :collapsed? is-collapsed?})
           children)))
