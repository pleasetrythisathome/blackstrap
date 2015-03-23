#_
(:require [om-bootstrap.button :as b]
          [om-bootstrap.mixins :as m]
          [rum])

(rum/defcs loading-button
  < (rum/local false :loading?) m/set-timeout-mixin
  [state]
  (let [loading? @(:loading? state)
        toggle #(swap! (:loading? state) not)
        handle-click (fn [e]
                       (toggle)
                       (m/set-timeout state toggle 2000))]
    (b/button {:bs-style "primary"
               :disabled? loading?
               :on-click (when-not loading?
                           handle-click)}
              (if loading?
                "Loading..."
                "Loading state"))))

(loading-button)
