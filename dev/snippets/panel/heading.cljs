#_
(:require [om-bootstrap.panel :as p])

[:div
 (p/panel {:header "Panel heading without title"}
          "Panel content")
 (p/panel {:header [:h3 "Panel title"]}
          "Panel content")]
