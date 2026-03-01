db = db.getSiblingDB('superapp');
db.ui_components.drop();

db.ui_components.insertMany([
  {
    "_id": "tpl_morning",
    "tag": "CAFFEINE",
    "type": "HeroButton",
    "props": { 
      "defaultGoal": "Get coffee quickly",
      "color": "#FF8C00",
      "action": "superapp://coffee"
    }
  },
  {
    "_id": "tpl_dinner",
    "tag": "DINNER",
    "type": "HeroButton",
    "props": { 
      "defaultGoal": "Order a comforting meal",
      "color": "#483D8B",
      "action": "superapp://dinner"
    }
  }
]);