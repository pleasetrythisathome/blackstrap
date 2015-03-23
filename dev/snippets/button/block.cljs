#_
(:require [om-bootstrap.button :as b])

[:div {:class "well"
        :style {:max-width 400
                :margin "0 auto 10px"}}
       (b/button {:bs-style "primary" :bs-size "large" :block? true}
                 "Block level button")
       (b/button {:bs-size "large" :block? true}
                 "Block level button")]
