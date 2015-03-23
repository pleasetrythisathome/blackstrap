#_
(:require [om-bootstrap.input :as i])

[:form
 (i/input {:type "text" :bs-style "success" :label "Success" :feedback? true})
 (i/input {:type "text" :bs-style "warning" :label "Warning" :feedback? true})
 (i/input {:type "text" :bs-style "error" :label "Error" :feedback? true})]
