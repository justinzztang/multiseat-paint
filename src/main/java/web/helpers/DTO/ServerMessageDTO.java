package web.helpers.DTO;

public class ServerMessageDTO {
    public String type;
    public int userID;

    public ServerMessageDTO(String type, int id){
        this.type = type;
        this.userID = id;
    }

}
