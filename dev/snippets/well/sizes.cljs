#_
(:require [om-bootstrap.random :as r])

[:div
 (r/well {:bs-size "large"} "Look, I'm in a large well!")
 (r/well {:bs-size "small"} "Look, I'm in a small well!")]
