(ns om-bootstrap.modal
  "IN PROGRESS work on a modal component. Depends on a fade mixin."
  (:require [om-bootstrap.mixins :as m]
            [om-bootstrap.types :as t]
            [schema.core :as s]
            [om-bootstrap.util :as u]
            [rum])
  (:require-macros [schema.macros :as sm]))

;; ## Schema

(def Modal
  "Options for the modal."
  {:header s/Any
   :footer s/Any
   (s/optional-key :keyboard?) s/Bool
   (s/optional-key :close-button?) s/Bool
   (s/optional-key :visible?) s/Bool
   (s/optional-key :animate?) s/Bool})

(def visible? (comp #(get-in % [:opts :visible?]) first :rum/args))

(rum/defcs modal*
  "Component that renders a Modal. Manages it's own toggle state"
  < (rum/local false :visible?)
  (m/default-local visible? :visible?)
  (m/transfer-args visible? :visible?)
  [state {:keys [opts children]}]
  (let [[bs props] (t/separate Modal opts {:bs-class "modal"})
        classes {:modal true
                 :fade true
                 :in @(:visible? state)}]
    [:div (u/merge-props props
                         {:class (u/class-set classes)})
     [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
       [:div {:class "modal-header"}
        (when (:close-button? bs)
          [:button {:type         "button"
                    :class        "close"
                    :aria-hidden  true
                    :on-click (fn [_]
                                (reset! (:visible? state) false))}
           "×"])
        (u/clone-with-props (:header bs) {:class "modal-title"})]
       [:div {:class "modal-body"}
        children]
       [:div {:class "modal-footer"}
        (:footer bs)]]]]))

(sm/defn modal
  [opts :- Modal & children]
  (modal* {:opts opts :children children}))
