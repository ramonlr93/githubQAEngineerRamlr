import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.*;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.LocalGregorianCalendar;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class testApiTrelloListsAndBoards {
    public String keyValue="7d278c4c444920a92cb7667f1b8b1f9a";
    public String tokenValue="ce4fbe4d415a98612b1c8d5f27156e27877e7e8d9f15c94bdd4af333061ab7e0";
    public String boardNameLists="Tablero de pruebas para Listas";
    public String boardId=null;
    public String idList=null;
    public String idCard=null;
    @BeforeTest
    public String createBoardForLists() throws InterruptedException {
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue,"name",boardNameLists)
                .when()
                .post("https://api.trello.com/1/boards/?key={key}&token={token}&name={name}").then().statusCode(200);

        String id= given().pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/members/me/boards?key={key}&token={token}").
                        then().statusCode(200).extract().path("id[0]");

        Thread.sleep(1000);
        boardId=id;
        return boardId;

    }
    @Parameters({ "listName"})
    @Test(priority = 1)
    public String createLists(String listName){
        String idListCreate=given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .when()
                .post("https://api.trello.com/1/lists?key={key}&token={token}&name="+listName+"&idBoard="+boardId+"")
                .then().statusCode(200).extract().path("id");
        idList=idListCreate;
        return idList;
    }
    @Parameters({ "listName"})
    @Test (priority=2)
    public void getListDetails(String listName){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/lists/"+idList+"?key={key}&token={token}").then().statusCode(200)
                .body("name",containsString(listName));
    }
    @Parameters({ "modifyNameList"})
    @Test (priority=3)
    public void modifyBoards(String modifyNameList){
        JSONObject request= new JSONObject();
        request.put("name",modifyNameList);
        given().header("Content-Type","application/json")
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(request.toJSONString()).pathParams("key",keyValue,"token",tokenValue)
                .put("https://api.trello.com/1/lists/"+idList+"?key={key}&token={token}&name=To Do Modified").
                then().statusCode(200);
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/lists/"+idList+"?key={key}&token={token}").then().statusCode(200)
                .body("name",containsString(modifyNameList));

    }
    //@Parameters({"nameCard","descCard","pos","dueComplete","due","urlSource"})
    @Parameters({"nameCard","descCard","pos","dueComplete","due","urlSource"})
    @Test(priority = 4)
    public String createCards(String nameCard, String descCard,String pos, String dueComplete, String due, String urlSource){

        String id=given().header("Content-Type","application/json")
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .pathParams("key",keyValue,"token",tokenValue).when()
       .post("https://api.trello.com/1/cards?key={key}&token={token}&idList="+idList+"" +
               "&name="+nameCard+"&desc="+descCard+"&pos="+pos+"&dueComplete="+dueComplete+"&due="+due+"&urlSource="+urlSource+"")
                .then().statusCode(200).extract().path("id");
        idCard=id;
        return idCard;
    }
    @Parameters({"nameCard"})
    @Test(priority = 5)
    public void getCardsInList(String nameCard){
       String response= given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/lists/"+idList+"/cards?key={key}&token={token}").then().statusCode(200)
               .extract().path("name[0]");
        Assert.assertEquals(response,nameCard);
    }
    @Parameters({ "modifyNameCard"})
    @Test (priority=6)
    public void modifyCards(String modifyNameCard){
        JSONObject request= new JSONObject();
        request.put("name",modifyNameCard);
        given().header("Content-Type","application/json")
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(request.toJSONString()).pathParams("key",keyValue,"token",tokenValue)
                .put("https://api.trello.com/1/cards/"+idCard+"?key={key}&token={token}&name="+modifyNameCard+"").
                then().statusCode(200);
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/cards/"+idCard+"?key={key}&token={token}").then().statusCode(200)
                .body("name",containsString(modifyNameCard));

    }
    @Parameters({ "modifyNameCard"})
    @Test (priority = 7)
    public void deleteCards(String modifyNameCard){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .when()
                .delete("https://api.trello.com/1/cards/"+idCard+"?key={key}&token={token}").then().statusCode(200);

        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .get("https://api.trello.com/1/cards/"+idCard+"?key={key}&token={token}").then().statusCode(404);
    }
    @AfterSuite
    public void deleteBoard(){
        given().contentType(ContentType.JSON).pathParams("key",keyValue,"token",tokenValue)
                .when()
                .delete("https://api.trello.com/1/boards/"+boardId+"?key={key}&token={token}").then().statusCode(200);


    }
}
