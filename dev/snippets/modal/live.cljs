#_
(:require [om-bootstrap.modal :as md]
          [rum])

(rum/defcs trigger
  < (rum/local false :visible?)
  [{:keys [visible?]}]
  [:div
   (md/modal {:header        [:h4 "This is a Modal"]
              :footer        [:div (b/button {} "Save")
                                    (b/button {} "Send")]
              :close-button? true
              :visible?      @visible?}
             "This is in the modal body")
   (b/button {:bs-style "primary"
              :bs-size "large"
              :on-click (fn [_] (reset! visible? true))}
             "Click to open Modal")])

(trigger)
