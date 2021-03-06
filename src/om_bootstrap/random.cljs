(ns om-bootstrap.random
  "Components that need to be filed, still."
  (:require [om-bootstrap.mixins :as m]
            [om-bootstrap.types :as t]
            [om-bootstrap.util :as u]
            [rum]
            [schema.core :as s :include-macros true]))

;; ## Jumbotron

(s/defn jumbotron :- t/Component
  "A lightweight, flexible component that can optionally extend the
   entire viewport to showcase key content on your site."
  [opts & children]
  [:div (u/merge-props opts {:class "jumbotron"})
   children])

;; ## Label

(s/defn label :- t/Component
  "Create a (label {} \"label!\") to show highlight information."
  [opts & children]
  (let [[bs props] (t/separate {} opts {:bs-class "label"
                                        :bs-style "default"})
        classes (t/bs-class-set bs)]
    [:span (u/merge-props props {:class (u/class-set classes)})
     children]))

;; ## Well

(s/defn well :- t/Component
  "Use the well as a simple effect on an element to give it an inset effect."
  [opts & children]
  (let [[bs props] (t/separate {} opts {:bs-class "well"})
        class (u/class-set (t/bs-class-set bs))]
    [:div (u/merge-props props {:class class})
     children]))

;; ## Header

(s/defn page-header :- t/Component
  "A simple shell for an h1 to appropriately space out and segment
  sections of content on a page. It can utilize the h1’s default small
  element, as well as most other components (with additional styles)."
  [opts & children]
  [:div (u/merge-props opts {:class "page-header"})
   [:h1 children]])

;; ## Tooltip

(def Placement
  (s/enum "top" "right" "bottom" "left"))

(def ToolTip
  (t/bootstrap
   {(s/optional-key :placement) Placement
    (s/optional-key :position-left) s/Int
    (s/optional-key :position-top) s/Int
    (s/optional-key :arrow-offset-left) s/Int
    (s/optional-key :arrow-offset-top) s/Int}))

(s/defn tooltip :- t/Component
  [opts :- ToolTip & children]
  (let [[bs _] (t/separate ToolTip opts {:placement "right"})
        classes {:tooltip true
                 (:placement bs) true
                 :in (or (:position-left bs)
                         (:position-top bs))}]
    [:div {:class (u/class-set classes)
           :style {:left (:position-left bs)
                   :top (:position-top bs)}}
     [:div {:class "tooltip-arrow"
            :style {:left (:arrow-offset-left bs)
                    :top (:arrow-offset-top bs)}}]
     [:div {:class "tooltip-inner"}
      children]]))

;; ## Alert

(def Alert
  (t/bootstrap
   {(s/optional-key :on-dismiss) (s/=> s/Any s/Any)
    (s/optional-key :dismiss-after) s/Int}))

(def alert-defaults
  {:bs-class "alert" :bs-style "info"})

(rum/defc alert*
  "Renders the alert component with timeout mixed in. TODO: This
   should probably use the component macro and be defined inline under
   the alert function. No need for a separate name."
  < rum/static m/set-timeout-mixin
  {:did-mount (fn [state]
                (let [bs (:bs (first (:rum/args state)))]
                  (if (and (:on-dismiss bs) (:dismiss-after bs))
                    (m/set-timeout state
                                   (:on-dismiss bs)
                                   (:dismiss-after bs))
                    state)))}
  [{:keys [bs props children]}]
  (let [classes (t/bs-class-set bs)
        dismiss-button (when-let [od (:on-dismiss bs)]
                         (:button {:type "button"
                                   :class "close"
                                   :on-click od
                                   :aria-hidden true}
                                  "&times;"))]
    [:div (u/merge-props props {:class (u/class-set classes)})
     dismiss-button
     children]))

(s/defn alert :- t/Component
  "Wrapper for the alert component to allow a better user interface."
  [opts :- Alert & children]
  (let [[bs props] (t/separate Alert opts alert-defaults)]
    (alert* {:bs bs
             :props props
             :children children})))

;; ## Popover

(def Popover
  (t/bootstrap
   {(s/optional-key :title) t/Renderable
    (s/optional-key :placement) Placement
    (s/optional-key :position-left) s/Int
    (s/optional-key :position-top) s/Int
    (s/optional-key :arrow-offset-left) s/Int
    (s/optional-key :arrow-offset-top) s/Int}))

;; TODO: Abstract out shared style generation between here and
;; tooltip.
(s/defn popover :- t/Component
  [opts :- Popover & children]
  (let [[bs _] (t/separate Popover opts {:placement "right"})
        classes {:popover true
                 (:placement bs) true
                 :in (or (:position-left bs)
                         (:position-top bs))}]
    [:div {:class (u/class-set classes)
           :style {:left (:position-left bs)
                   :top (:position-top bs)
                   :display "block"}}
     [:div {:class "arrow"
            :style {:left (:arrow-offset-left bs)
                    :top (:arrow-offset-top bs)}}]
     (when-let [title (:title bs)]
       [:h3 {:class "popover-title"} title])
     [:div {:class "popover-content"}
      children]]))

;; ## Badge

(def Badge
  (t/bootstrap
   {(s/optional-key :pull-right?) s/Bool}))

(s/defn badge :- t/Component
  [opts :- Badge & children]
  (let [[bs props] (t/separate Badge opts)
        classes {:pull-right (:pull-right? bs)
                 :badge (u/some-valid-component? children)}]
    [:span (u/merge-props props {:class (u/class-set classes)})
     children]))

;; ## Glyphicon

(def Glyphicon
  (t/bootstrap {:glyph s/Str}))

(s/defn glyphicon :- t/Component
  [opts :- Glyphicon & children]
  (let [[bs props] (t/separate Glyphicon opts {:bs-class "glyphicon"})
        classes (assoc (t/bs-class-set bs)
                       (str "glyphicon-" (:glyph bs)) true)]
    [:span (u/merge-props props {:class (u/class-set classes)})
     children]))
