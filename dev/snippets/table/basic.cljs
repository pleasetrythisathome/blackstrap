#_
(:require [om-bootstrap.table :refer [table]])

(table {:striped? true :bordered? true :condensed? true :hover? true}
       [:thead
        [:tr
         [:th "#"]
         [:th "First Name"]
         [:th "Last Name"]
         [:th "Username"]]]
       [:tbody
        [:tr
         [:td "1"]
         [:td "Mark"]
         [:td "Otto"]
         [:td "@mdo"]]
        [:tr
         [:td "2"]
         [:td "Jacob"]
         [:td "Thornton"]
         [:td "@fat"]]
        [:tr
         [:td "3"]
         [:td {:col-span 2} "Larry the Bird"]
         [:td "@twitter"]]])
