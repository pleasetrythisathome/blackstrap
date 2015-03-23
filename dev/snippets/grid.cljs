#_
(:require [om-bootstrap.grid :as g])

[:div
 {:class "grids-examples"}
 (g/grid {}
         (g/row {:class "show-grid"}
                (g/col {:xs 12 :md 8}
                       [:code {} "(g/col {:xs 12 :md 8})"])
                (g/col {:xs 6 :md 4}
                       [:code {} "(g/col {:xs 6 :md 4})"]))
         (g/row {:class "show-grid"}
                (g/col {:xs 6 :md 4}
                       [:code {} "(g/col {:xs 6 :md 4})"])
                (g/col {:xs 6 :md 4}
                       [:code {} "(g/col {:xs 6 :md 4})"])
                (g/col {:xs 6 :md 4}
                       [:code {} "(g/col {:xs 6 :md 4})"]))
         (g/row {:class "show-grid"}
                (g/col {:xs 6 :xs-offset 6}
                       [:code {} "(g/col {:xs 6 :xs-offset 6})"]))
         (g/row {:class "show-grid"}
                (g/col {:md 6 :md-push 6}
                       [:code {} "(g/col {:md 6 :md-push 6})"])
                (g/col {:md 6 :md-pull 6}
                       [:code {} "(g/col {:md 6 :md-push 6})"])))]
