package ca.acoii.nationbuilder.nationbuilder;

public class EventOption {
    private String option;
    private EventResult result;
    public EventOption() {
        option = "";
        result = null;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setResult(EventResult result) {
        this.result = result;
    }

    public String getOption() {
        return option;
    }

    public EventResult getResult() {
        return result;
    }
}
