package fr.milekat.MCPG_Discord.classes;

import java.util.ArrayList;

public class Step {
    private final String name;
    private final String type;
    private final String message;
    private final Long min;
    private final Long max;
    private final String yes;
    private final String no;
    private final String back;
    private final ArrayList<String> choices;
    private final String next;
    private final boolean save;

    /**
     * Empty step constructor
     */
    public Step() {
        this.name = null;
        this.type = null;
        this.message = null;
        this.min = 0L;
        this.max = 0L;
        this.yes = null;
        this.no = null;
        this.back = null;
        this.choices = null;
        this.next = null;
        this.save = false;
    }

    /**
     * INIT type step constructor
     */
    public Step(String name, String type, String message, String next, boolean save) {
        this.name = name;
        this.type = type;
        this.message = message;
        this.min = 0L;
        this.max = 0L;
        this.yes = null;
        this.no = null;
        this.back = null;
        this.choices = null;
        this.next = next;
        this.save = save;
    }

    /**
     * TEXT type step constructor
     */
    public Step(String name, String type, String message, Long min, Long max, String next, boolean save) {
        this.name = name;
        this.type = type;
        this.message = message;
        this.min = min;
        this.max = max;
        this.yes = null;
        this.no = null;
        this.back = null;
        this.choices = null;
        this.next = next;
        this.save = save;
    }

    /**
     * VALID type step constructor
     */
    public Step(String name, String type, String message, String yes, String no, String back, String next, boolean save) {
        this.name = name;
        this.type = type;
        this.message = message;
        this.min = 0L;
        this.max = 0L;
        this.yes = yes;
        this.no = no;
        this.back = back;
        this.choices = null;
        this.next = next;
        this.save = save;
    }

    /**
     * CHOICES type step constructor
     */
    public Step(String name, String type, String message, ArrayList<String> choices, String next, boolean save) {
        this.name = name;
        this.type = type;
        this.message = message;
        this.min = 0L;
        this.max = 0L;
        this.yes = null;
        this.no = null;
        this.back = null;
        this.choices = choices;
        this.next = next;
        this.save = save;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Long getMin() {
        return min;
    }

    public Long getMax() {
        return max;
    }

    public String getYes() {
        return yes;
    }

    public String getNo() {
        return no;
    }

    public String getBack() {
        return back;
    }

    public ArrayList<String> getChoices() {
        return choices;
    }

    public String getNext() {
        return next;
    }

    public boolean isSave() {
        return save;
    }
}
