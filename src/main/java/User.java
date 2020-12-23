import lombok.AllArgsConstructor;
import lombok.Getter;

    @AllArgsConstructor
    public enum User {

        Manager(
                "manager",
                "manager",
                "Менеджер");


        @Getter
        private final String login;
        @Getter
        private final String password;
        @Getter
        private final String shortDescription;
    }

