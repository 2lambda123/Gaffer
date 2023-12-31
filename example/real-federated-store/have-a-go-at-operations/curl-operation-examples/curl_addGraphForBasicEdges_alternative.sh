curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
    "class": "AddGraph",
    "graphId": "mapEdges",
    "storeProperties": {
      "gaffer.store.class":"uk.gov.gchq.gaffer.mapstore.MapStore"
    },
    "schema": {
         "edges": {
             "BasicEdge": {
               "source": "vertex",
               "destination": "vertex",
               "directed": "true",
               "properties": {
                 "count": "count"
               }
             }
           },

       "types": {
         "vertex": {
           "class": "java.lang.String"
         },
         "count": {
           "class": "java.lang.Integer",
           "aggregateFunction": {
             "class": "uk.gov.gchq.koryphe.impl.binaryoperator.Sum"
           }
         },
         "true": {
           "description": "A simple boolean that must always be true.",
           "class": "java.lang.Boolean",
           "validateFunctions": [
             {
               "class": "uk.gov.gchq.koryphe.impl.predicate.IsTrue"
             }
           ]
         }
       }
    },
    "isPublic": true
 }' 'http://localhost:8080/rest/graph/operations/execute'
