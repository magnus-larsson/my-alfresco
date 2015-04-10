// @overridden projects/slingshot/source/web/components/invite/invitationlist.js
//TODO remove these customizations when we go to 5.0. This customization adds the arrow in the drop down menu in the setRoleForRecord function

(function (_getRoleLabel) {
  Alfresco.InvitationList.prototype.getRoleLabel = function(record) {
    var label = _getRoleLabel.call(this, record);
    if (record.getData("role") === undefined) {
    label += " " + Alfresco.constants.MENU_ARROW_SYMBOL;
    }
    return label;
  };
}(Alfresco.InvitationList.prototype.getRoleLabel));