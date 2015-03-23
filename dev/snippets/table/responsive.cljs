#_
(:require [om-bootstrap.table :refer [table]])

(table {:responsive? true}
       [:thead
        [:tr
         [:th "#"]
         (repeat 6 [:th "Table heading"])]
        [:tbody
         (for [i (range 3)]
           [:tr
            [:td (str (inc i))]
            (repeat 6 [:td "Table cell"])])]])
