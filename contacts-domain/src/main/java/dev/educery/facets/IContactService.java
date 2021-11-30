package dev.educery.facets;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.webcohesion.enunciate.metadata.rs.*;
import dev.educery.context.SpringContext;

import dev.educery.domain.Contact;
import dev.educery.domain.ItemBrief;
import dev.educery.domain.ItemPart;

/**
 * Maintains details for each registered Contact.
 *
 * @author nik <nikboyd@sonic.net>
 */
public interface IContactService {

    public static final String BasePath = "/";
    public static final String ID = "id";
    public static final String IdPath = "/{" + ID + "}";

    public static final String ItemPath = "/contacts";
    public static final String ItemIdPath = ItemPath + IdPath;
    public static final String CheckPath = ItemPath + "/check";
    public static final String FirstPath = ItemPath + "/first";
    public static final String HashIdPath = ItemPath + "/hash";
    public static final String CountPath = ItemPath + "/count";
    public static final String BriefPath = ItemPath + "/briefs";
    public static final String PartPath  = ItemPath + "/part";

    public static final String Type = "idType";
    public static final String Value = "contactID";
    public static final String Name = "name";
    public static final String City = "city";
    public static final String Zip = "zip";

    /**
     * Counts saved contacts.
     * @return Contains a count of the saved contacts.
     */
    @GET
    @Path(CountPath)
    @TypeHint(ItemBrief.class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "a count of contacts")})
    public Response countItems();

    /**
     * Finds the first contact alphabetically.
     * @return Contains the first contact.
     */
    @GET
    @Path(FirstPath)
    @TypeHint(Contact.class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "first contact"),
        @ResponseCode(code = 410, condition = "missing contact")})
    public Response findFirstContact();

    /**
     * Lists briefs of selected contacts.
     * @param name a contact full name or name pattern
     * @return Contains a list of the selected contact briefs.
     */
    @GET
    @Path(BriefPath)
    @TypeHint(ItemBrief[].class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "selected contact briefs")})
    public Response listBriefs(
        @QueryParam(Name) String name);

    /**
     * Lists the selected contacts.
     * @param name a contact full name or name pattern
     * @param city a city name or pattern
     * @param zip a zip code
     * @return Contains a list of the selected contacts.
     */
    @GET
    @Path(ItemPath)
    @TypeHint(Contact[].class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "selected contacts")})
    public Response listItems(
        @QueryParam(Name) String name,
        @QueryParam(City) String city,
        @QueryParam(Zip) String zip);

    /**
     * Checks parts of a contact for duplications.
     * @param itemJSON contains contact details
     * @return Contains any messages about duplicates.
     */
    @POST
    @Path(CheckPath)
    @TypeHint(String[].class)
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 410, condition = "missing contact"),
        @ResponseCode(code = 409, condition = "duplication problems"),
        @ResponseCode(code = 200, condition = "validated the contact parts")})
    public Response checkParts(
        @TypeHint(Contact.class) String itemJSON);

    /**
     * Creates a contact part and associates it.
     * @param partJSON contains contact part details
     * @return Contains the new part ID.
     */
    @POST
    @Path(PartPath)
    @TypeHint(ItemBrief.class)
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 410, condition = "missing contact"),
        @ResponseCode(code = 409, condition = "validation problems"),
        @ResponseCode(code = 201, condition = "created a contact part")})
    public Response createPart(
        @TypeHint(ItemPart.class) String partJSON);

    /**
     * Creates and registers a new contact.
     * @param itemJSON contains contact details
     * @return Contains the contact IDs usable for retrieval.
     */
    @POST
    @Path(ItemPath)
    @TypeHint(ItemBrief.class)
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 409, condition = "validation problems"),
        @ResponseCode(code = 201, condition = "created a contact")})
    public Response createItem(
        @TypeHint(Contact.class) String itemJSON);

    /**
     * Saves changes to an existing contact.
     * @param itemJSON contains contact details
     * @return Contains the contact IDs usable for retrieval.
     */
    @PUT
    @Path(ItemPath)
    @TypeHint(ItemBrief.class)
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "saved a contact"),
        @ResponseCode(code = 409, condition = "missing contact ID"),
        @ResponseCode(code = 410, condition = "missing contact")})
    public Response saveItem(
        @TypeHint(Contact.class) String itemJSON);

    /**
     * Gets a registered contact.
     * @param itemID identifies a contact
     * @return Contains the details of a contact (if registered).
     */
    @GET
    @Path(ItemIdPath)
    @TypeHint(Contact.class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "found a contact"),
        @ResponseCode(code = 410, condition = "missing contact")})
    public Response getItem(
        @PathParam(ID) long itemID);

    /**
     * Gets a registered contact with lookup based on hash of the supplied ID.
     * <ul>
     * <li>idType='name' looks up a contact by itemID='contact name'</li>
     * <li>idType='phone' looks up a contact by itemID='phone number'</li>
     * <li>idType='email' looks up a contact by itemID='email address'</li>
     * </ul>
     * @param idType indicates a kind of ID
     * @param itemID identifies a contact
     * @return Contains the details of a contact (if registered).
     */
    @GET
    @Path(HashIdPath)
    @TypeHint(Contact.class)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "found a contact"),
        @ResponseCode(code = 410, condition = "missing contact")})
    public Response getItemWithHash(
        @QueryParam(Type) Contact.Type idType,
        @QueryParam(Value) String itemID);

    /**
     * Deletes a registered contact.
     * @param itemID identifies a contact
     * @return Indicates whether a contact was deleted.
     */
    @DELETE
    @Path(ItemIdPath)
    @TypeHint(List.class)
    @StatusCodes({
        @ResponseCode(code = 200, condition = "deleted a contact"),
        @ResponseCode(code = 202, condition = "no contact found")})
    public Response deleteItem(
        @PathParam(ID) long itemID);

    /**
     * Deletes a registered contact with lookup based on hash of the supplied ID.
     * <ul>
     * <li>idType='name' looks up a contact by itemID='contact name'</li>
     * <li>idType='phone' looks up a contact by itemID='phone number'</li>
     * <li>idType='email' looks up a contact by itemID='email address'</li>
     * </ul>
     * @param idType indicates a kind of ID
     * @param itemID identifies a contact
     * @return Indicates whether a contact was deleted.
     */
    @DELETE
    @Path(HashIdPath)
    @Produces({MediaType.APPLICATION_JSON})
    @StatusCodes({
        @ResponseCode(code = 200, condition = "deleted a contact"),
        @ResponseCode(code = 202, condition = "no contact found")})
    public Response deleteItemWithHash(
        @QueryParam(Type) Contact.Type idType,
        @QueryParam(Value) String itemID);

    static final String ConfigurationFile = "/service-client.xml";
    static public IContactService loadProxy() {
        return SpringContext.named(ConfigurationFile).getBean(IContactService.class); }

} // IContactService
