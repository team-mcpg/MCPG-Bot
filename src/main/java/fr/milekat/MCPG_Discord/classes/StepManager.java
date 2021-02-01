package fr.milekat.MCPG_Discord.classes;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StepManager {
    /**
     * Get "register steps" from config.json file !
     */
    public static HashMap<String, Step> getSteps(JSONObject config) {
        HashMap<String, Step> steps = new HashMap<>();
        for (Object node : (JSONArray) config.get("register_steps")) {
            JSONObject jsonObject = (JSONObject) node;
            Step step = new Step();
            switch ((String) jsonObject.get("type")) {
                case "INIT": {
                    step = new Step((String) jsonObject.get("type"),
                            (String) jsonObject.get("next"),
                            (boolean) jsonObject.get("save"));
                    break;
                }
                case "TEXT": {
                    step = new Step((String) jsonObject.get("name"),
                            (String) jsonObject.get("type"),
                            (String) jsonObject.get("message"),
                            (int) jsonObject.get("min_chars"),
                            (int) jsonObject.get("max_chars"),
                            (String) jsonObject.get("next"),
                            (boolean) jsonObject.get("save"));
                    break;
                }
                case "END":
                case "VALID": {
                    step = new Step((String) jsonObject.get("name"),
                            (String) jsonObject.get("type"),
                            (String) jsonObject.get("message"),
                            (String) jsonObject.get("yes"),
                            (String) jsonObject.get("no"),
                            (String) jsonObject.get("return_step"),
                            (String) jsonObject.get("next"),
                            (boolean) jsonObject.get("save"));
                    break;
                }
                case "CHOICES": {
                    ArrayList<String> choices = new ArrayList<>();
                    for (Object choice : (JSONArray) jsonObject.get("choices")) {
                        choices.add((String) choice);
                    }
                    step = new Step((String) jsonObject.get("name"),
                            (String) jsonObject.get("type"),
                            (String) jsonObject.get("message"),
                            choices,
                            (String) jsonObject.get("next"),
                            (boolean) jsonObject.get("save"));
                    break;
                }
            }
            steps.put(step.getName(), step);
        }
        return steps;
    }
}
