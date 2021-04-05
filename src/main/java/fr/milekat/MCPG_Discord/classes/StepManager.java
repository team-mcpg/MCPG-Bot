package fr.milekat.MCPG_Discord.classes;

import fr.milekat.MCPG_Discord.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StepManager {
    /**
     * Get "register steps" from config.json file !
     */
    public static HashMap<String, Step> getSteps(JSONArray configStep) {
        HashMap<String, Step> steps = new HashMap<>();
        for (Object node : configStep) {
            JSONObject jsonObject = (JSONObject) node;
            if (((String) jsonObject.get("name")).contains("__")) continue;
            if (Main.DEBUG_ERROR) Main.log("Load step: " + jsonObject.get("name"));
            Step step = new Step();
            switch ((String) jsonObject.get("type")) {
                case "INIT": {
                    step = new Step((String) jsonObject.get("name"),
                            (String) jsonObject.get("type"),
                            (String) jsonObject.get("message"),
                            (String) jsonObject.get("next"),
                            (boolean) jsonObject.get("save"));
                    break;
                }
                case "TEXT": {
                    step = new Step((String) jsonObject.get("name"),
                            (String) jsonObject.get("type"),
                            (String) jsonObject.get("message"),
                            (Long) jsonObject.get("min_chars"),
                            (Long) jsonObject.get("max_chars"),
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
