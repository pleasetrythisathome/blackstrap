#_
(:require [om-bootstrap.button :as b])

[:div
 (b/toolbar {}
            (b/button {:bs-style "primary" :bs-size "large"} "Large button")
            (b/button {:bs-size "large"} "Large button"))
 (b/toolbar {}
            (b/button {:bs-style "primary"} "Default button")
            (b/button {} "Default button"))
 (b/toolbar {}
            (b/button {:bs-style "primary" :bs-size "small"} "Small button")
            (b/button {:bs-size "small"} "Small button"))
 (b/toolbar {}
            (b/button {:bs-style "primary" :bs-size "xsmall"} "Extra small button")
            (b/button {:bs-size "xsmall"} "Extra small button"))]
