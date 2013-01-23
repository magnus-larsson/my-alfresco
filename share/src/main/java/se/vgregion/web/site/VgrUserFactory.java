package se.vgregion.web.site;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.alfresco.web.site.SlingshotUserFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AlfrescoUser;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;

public class VgrUserFactory extends SlingshotUserFactory {

  public static final String RESPONSIBILITY_CODE = "{http://www.vgregion.se/model/1.0}responsibility_code";
  public static final String ORGANIZATION_DN = "{http://www.vgregion.se/model/1.0}organization_dn";
  public static final String PROP_RESPONSIBILITY_CODE = "responsibility_code";
  public static final String PROP_ORGANIZATION_DN = "organization_dn";

  @Override
  protected AlfrescoUser constructUser(final JSONObject properties, final Map<String, Boolean> capabilities, final Map<String, Boolean> immutability)
      throws JSONException {
    final AlfrescoUser user = super.constructUser(properties, capabilities, immutability);

    user.setProperty(PROP_RESPONSIBILITY_CODE, properties.has(RESPONSIBILITY_CODE) ? properties.getString(RESPONSIBILITY_CODE) : null);
    user.setProperty(PROP_ORGANIZATION_DN, properties.has(ORGANIZATION_DN) ? properties.getString(ORGANIZATION_DN) : null);

    return user;
  }

  @Override
  public void saveUser(final AlfrescoUser user) throws UserFactoryException {
    final RequestContext context = ThreadLocalRequestContext.getRequestContext();

    if (!context.getUserId().equals(user.getId())) {
      throw new UserFactoryException("Unable to persist user with different Id that current Id.");
    }

    final StringBuilderWriter buf = new StringBuilderWriter(512);
    final JSONWriter writer = new JSONWriter(buf);

    try {
      writer.startObject();

      writer.writeValue("username", user.getId());

      writer.startValue("properties");
      writer.startObject();
      writer.writeValue(CM_FIRSTNAME, user.getFirstName());
      writer.writeValue(CM_LASTNAME, user.getLastName());
      writer.writeValue(CM_JOBTITLE, user.getJobTitle());
      writer.writeValue(CM_ORGANIZATION, user.getOrganization());
      writer.writeValue(CM_LOCATION, user.getLocation());
      writer.writeValue(CM_EMAIL, user.getEmail());
      writer.writeValue(CM_TELEPHONE, user.getTelephone());
      writer.writeValue(CM_MOBILE, user.getMobilePhone());
      writer.writeValue(CM_SKYPE, user.getSkype());
      writer.writeValue(CM_INSTANTMSG, user.getInstantMsg());
      writer.writeValue(CM_GOOGLEUSERNAME, user.getGoogleUsername());
      writer.writeValue(CM_COMPANYADDRESS1, user.getCompanyAddress1());
      writer.writeValue(CM_COMPANYADDRESS2, user.getCompanyAddress2());
      writer.writeValue(CM_COMPANYADDRESS3, user.getCompanyAddress3());
      writer.writeValue(CM_COMPANYPOSTCODE, user.getCompanyPostcode());
      writer.writeValue(CM_COMPANYFAX, user.getCompanyFax());
      writer.writeValue(CM_COMPANYEMAIL, user.getCompanyEmail());
      writer.writeValue(CM_COMPANYTELEPHONE, user.getCompanyTelephone());

      // START VGR specific properties
      writer.writeValue(RESPONSIBILITY_CODE, user.getStringProperty(PROP_RESPONSIBILITY_CODE));
      writer.writeValue(ORGANIZATION_DN, user.getStringProperty(PROP_ORGANIZATION_DN));
      // END VGR specific properties

      writer.endObject();
      writer.endValue();

      writer.startValue("content");
      writer.startObject();
      writer.writeValue(CM_PERSONDESCRIPTION, user.getBiography());
      writer.endObject();
      writer.endValue();

      writer.endObject();

      final Connector conn = FrameworkUtil.getConnector(context, ALFRESCO_ENDPOINT_ID);

      final ConnectorContext c = new ConnectorContext(HttpMethod.POST);

      c.setContentType("application/json");

      final Response res = conn.call("/slingshot/profile/userprofile", c, new ByteArrayInputStream(buf.toString().getBytes()));

      if (Status.STATUS_OK != res.getStatus().getCode()) {
        throw new UserFactoryException("Remote error during User save: " + res.getStatus().getMessage());
      }
    } catch (final IOException ioErr) {
      throw new UserFactoryException("IO error during User save: " + ioErr.getMessage(), ioErr);
    } catch (final ConnectorServiceException cse) {
      throw new UserFactoryException("Configuration error during User save: " + cse.getMessage(), cse);
    }
  }
}
