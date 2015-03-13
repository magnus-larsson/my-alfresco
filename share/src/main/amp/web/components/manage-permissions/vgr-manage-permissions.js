// @overridden projects/slingshot/source/web/components/manage-permissions/manage-permissions.js

(function(_onPermissionsLoaded) {
  /**
   * Success handler called when the AJAX call to the doclist permissions web script returns successfully
   *
   * @method onPermissionsLoaded
   * @param response {object} Ajax response details
   */
  /**
   * Customized to filter out non-site-roles
   */
  Alfresco.component.ManagePermissions.prototype.onPermissionsLoaded = function Permissions_onPermissionsLoaded(response)	{
    var data = response.json;

    // Update local copy of permissions
    this.permissions =
    {
      originalIsInherited: data.isInherited,
      isInherited: data.isInherited,
      canReadInherited: data.canReadInherited,
      inherited: data.inherited,
      original: Alfresco.util.deepCopy(data.direct),
      current: Alfresco.util.deepCopy(data.direct)
    };

    // Does the user have permissions to read the parent node's permissions?
    if (!this.permissions.canReadInherited)
    {
      this.widgets.dtInherited.set("MSG_EMPTY", this.msg("message.empty.no-permission"));
    }

    // Need the inheritance warning?
    this.inheritanceWarning = !data.isInherited;

    //Patch begin
    var newSettable = new Array();
    for (var i=0; i<data.settable.length;i++) {
      if (data.settable[i].startsWith("Site")) {
        newSettable.push(data.settable[i]);
      }
    }
    data.settable = newSettable;
    //Patch end

    // Roles the user is allowed to select from
    this.settableRoles = data.settable;
    this.settableRolesMenuData = [];
    for (var i = 0, ii = data.settable.length; i < ii; i++)
    {
      this.settableRoles[data.settable[i]] = true;
      this.settableRolesMenuData.push(
      {
        text: data.settable[i],
        value: data.settable[i]
      });
    }

    this.deferredReady.fulfil("onPermissionsLoaded");
	};
}(Alfresco.component.ManagePermissions.prototype.onPermissionsLoaded));
