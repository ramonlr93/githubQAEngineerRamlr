import io.restassured.http.ContentType;
import io.restassured.internal.common.assertion.Assertion;
import io.restassured.response.ValidatableResponse;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.containsString;

public class testApiTrelloBoards {
    public String keyValue="7d278c4c444920a92cb7667f1b8b1f9a";
    public String tokenValue="ce4fbe4d415a98612b1c8d5f27156e27877e7e8d9f15c94bdd4af333061ab7e0";
    public String boardName="Tablero pruebas auto";
    public String boardId=null;

    @Test (priority=1)
    public void createBoard(){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue,"name",boardName)
                .when()
                .post("https://api.trello.com/1/boards/?key={key}&token={token}&name={name}").then().statusCode(200);

    }

    @Test (priority=2)
    public String getBoards() throws InterruptedException {
       String id= given().pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/members/me/boards?key={key}&token={token}").
                then().statusCode(200).extract().path("id[0]");

       Thread.sleep(1000);
       boardId=id;
        return boardId;
    }
    @Test (priority=3)
    public void getBoardDetails(){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/boards/"+boardId+"?key={key}&token={token}").then().statusCode(200)
                .body("name",containsString(boardName));
    }
    @Test (priority=4)
    public void getListOnTheBoards(){
        given().pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/boards/"+boardId+"/lists?key={key}&token={token}")
                .then().statusCode(200).body("name",hasItems("Lista de tareas","En proceso","Hecho"));

    }
    @Parameters({ "modifyNameBoard","modifyDescBoard" })
    @Test (priority=5)
    public void modifyBoards(String modifyNameBoard, String modifyDescBoard){
        JSONObject request= new JSONObject();
        request.put("name",modifyNameBoard);
        request.put("desc",modifyDescBoard);
         given().header("Content-Type","application/json")
                 .contentType(ContentType.JSON).accept(ContentType.JSON)
                 .body(request.toJSONString()).pathParams("key",keyValue,"token",tokenValue)
                .put("https://api.trello.com/1/boards/"+boardId+"?key={key}&token={token}").
                        then().statusCode(200);
         given().pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/boards/"+boardId+"?key={key}&token={token}").
                then().statusCode(200).body("name",containsString(modifyNameBoard));






    }
   @AfterSuite
    public void deleteBoard(){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .when()
                .delete("https://api.trello.com/1/boards/"+boardId+"?key={key}&token={token}").then().statusCode(200);


    }
}
