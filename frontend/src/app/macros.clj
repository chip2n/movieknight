(ns app.macros)

(defmacro pipe-events [type dest]
  (let [name (gensym "pipeline")]
    `(defonce ~name
       (cljs.core.async/pipeline 1 ~dest (filter (comp #(= ~type %) :type)) app.ui/event-chan))))
