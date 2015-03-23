#_
(:require [om-bootstrap.panel :as p])

(for [style [nil "primary" "success" "info" "warning" "danger"]]
  (p/panel (merge {:header [:h3 "Panel title"]}
                  (when style {:bs-style style}))
           "Panel content"))
