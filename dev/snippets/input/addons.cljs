#_
(:require [om-bootstrap.input :as i])

[:form
 (i/input {:type "text" :addon-before "@"})
 (i/input {:type "text" :addon-after ".00"})
 (i/input {:type "text" :addon-before "$" :addon-after ".00"})]
