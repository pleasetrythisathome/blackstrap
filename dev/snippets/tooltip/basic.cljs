#_
(:require [om-bootstrap.random :as r])

[:div {:style {:height 50}}
 (r/tooltip {:placement "right"
             :position-left 150
             :position-top 50}
            [:strong "Holy guacamole!"]
            " Check this info.")]
