#_
(:require [om-bootstrap.panel :as p])

(p/panel
 {:header "List group panel"
  :list-group [:ul {:class "list-group"}
               [:li {:class "list-group-item"} "Item 1"]
               [:li {:class "list-group-item"} "Item 2"]
               [:li {:class "list-group-item"} "Item 3"]]}
 nil)
