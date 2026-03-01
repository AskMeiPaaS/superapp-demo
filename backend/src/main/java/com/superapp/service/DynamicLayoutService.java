package com.superapp.service;

import com.superapp.model.UIComponent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;

@Service
public class DynamicLayoutService {
    private final MongoTemplate mongoTemplate;
    private final UICopywriter copywriter;

    public DynamicLayoutService(MongoTemplate mongoTemplate, UICopywriter copywriter) {
        this.mongoTemplate = mongoTemplate;
        this.copywriter = copywriter;
    }

    public List<UIComponent> getLayout(String context) {
        // 1. Simple rule to pick template (Morning vs Evening)
        int hour = LocalTime.now().getHour();
        String tag = (hour < 12) ? "CAFFEINE" : "DINNER";

        // 2. Fetch Template
        UIComponent template = mongoTemplate.findOne(
            new Query(Criteria.where("tag").is(tag)), UIComponent.class
        );

        if (template != null) {
            // 3. AI generates the label in real-time
            String goal = (String) template.props().get("defaultGoal");
            String aiLabel = copywriter.generateLabel(context, goal);
            
            // 4. Update the label in memory (not DB)
            template.props().put("label", aiLabel);
            return List.of(template);
        }
        return List.of();
    }
}