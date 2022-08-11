import java.util.Objects;

public class Courier {

    private long chatId;
    private String name, username;
    private Privilege privilege;

    public Courier(long chatId, String username, String name, Privilege privilege) {
        this.chatId = chatId;
        this.name = name;
        this.privilege = privilege;
        this.username = username;
    }

    //getters
    public long getChatId() {
        return chatId;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    //setters
    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Courier courier = (Courier) o;
        return chatId == courier.chatId && Objects.equals(name, courier.name) && Objects.equals(username, courier.username) && Objects.equals(privilege, courier.privilege);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, name, username, privilege);
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" +
                "Username: " + username + "\n" +
                "Chat Id: " + chatId + "\n" +
                "Rights: " + privilege.name() + "\n";
    }
}
