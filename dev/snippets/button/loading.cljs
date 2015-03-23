#_
(:require [om-bootstrap.button :as b]
          [om-bootstrap.mixins :as m]
          [rum])

(rum/defcs loading-button
  < (rum/local {:loading? false}) m/set-timeout-mixin
  [state]
  (let [{:keys [loading?]} @(:rum/local state)
        toggle #(swap! (:rum/local state) update :loading? not)

        ;; This is required to get around
        ;; https://github.com/Prismatic/om-tools/issues/29.
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
