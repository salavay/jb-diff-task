<#-- @ftlvariable name="diff_text" type="java.util.List<Diff.StringWithType>" -->
<#-- @ftlvariable name="old_text" type="java.util.List<Diff.StringWithType>" -->
<#-- @ftlvariable name="new_text" type="java.util.List<Diff.StringWithType>" -->
<#-- @ftlvariable name="isTripleView" type="java.lang.Boolean" -->
<#import "commons.ftlh" as c>


<@c.page old_text
diff_text
new_text!("")
isTripleView?string("styleTripleView.css", "styleSplitView.css")>

</@c.page>