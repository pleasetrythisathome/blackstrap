#_
(:require [om-bootstrap.button :as b]
          [om-bootstrap.random :as r])

(r/jumbotron {}
             [:h1 "Hello, World!"]
             [:p "This is a simple hero unit, a simple
             jumbotron-style component for calling extra attention to
             featured content or information."]
             [:p (b/button {:bs-style "primary"} "Learn More")])
