#_
(:require [om-bootstrap.input :as i]
          [rum])

(defn validation-state
  "Returns a Bootstrap :bs-style string based on the supplied string
  length."
  [s]
  (let [l (count s)]
    (cond (> l 10) "success"
          (> l 5) "warning"
          (pos? l) "error"
          :else nil)))

(rum/defcs example-input
  < (rum/local "" :text)
  [state]
  (i/input
   {:feedback? true
    :type "text"
    :label "Working example with validation"
    :placeholder "Enter text"
    :help "Validates based on string length."
    :group-classname "group-class"
    :wrapper-classname "wrapper-class"
    :label-classname "label-class"
    :value @(:text state)
    :bs-style (validation-state @(:text state))
    :on-change #(reset! (:text state) (.. %  -target -value))}))

(example-input)
