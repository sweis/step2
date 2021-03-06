<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<%--
 ~ Copyright 2009 Google Inc.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~
  --%>
<%@ page session="true" %>
<%@page import="com.google.step2.example.consumer.servlet.LoginViaPopupServlet"%>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <title>Pop-up Example</title>

    <style type="text/css">
      #popupForm {
        text-align:center;
        margin-bottom:4px;
        margin-top:10px;
      }
      #welcomePane {
        float: left;
        width: 40%;
      }
      #libPane {
        float: left;
        width: 60%;
      }
      #results {
        font-family:sans-serif;
        font-size:14px;
        text-align:left;
      }
     body {
       font-family:sans-serif;
     }

    </style>

  </head>
  <body style = "background-color:#FFFFFF; color:#000000;">
      <%
        String returnToUrl = (String) session.getAttribute(LoginViaPopupServlet.RETURN_TO);
        String realm = (String) session.getAttribute(LoginViaPopupServlet.REALM);
        realm = (null == realm) ? returnToUrl : realm;
        String endpoint = (String) session.getAttribute(LoginViaPopupServlet.OP_ENDPOINT);
        String buttonImage = (String) session.getAttribute(LoginViaPopupServlet.BUTTON_IMAGE);
        String opFriendlyName = (String) session.getAttribute(LoginViaPopupServlet.OP_FRIENDLY);
        String extensionParameters = (String) session.getAttribute(LoginViaPopupServlet.EXTENSION_PARAMS);
      %>
    <script type="text/javascript" src="popuplib.js"></script>
    <script type="text/javascript" src="jquery-1.3.1.js"></script>

<center><h2>OpenID Popup Demo</h2></center>

    <div id="popupForm">
      <div id="welcomePane">
        <button name = "submit_button" id = "submit_button"
                style = "background-color:transparent;border-width:1px;border-color:#000000;padding:3px;margin-top:200px;">
          <img src="<%= buttonImage %>" alt="<%= opFriendlyName %>" style="margin-bottom:-3px;"/>&nbsp; Sign in using your Google Account
        </button>
        <br />
        <input type="checkbox" id="stayOnPage" name="stayOnPage" checked="checked">
        <label for="stayOnPage">stay on this page after login</label>
        <br /><br />
        For examples on how to use the library, see <a href="http://step2.googlecode.com">step2.googlecode.com</a>
      </div>

      <div id="libPane">
           Popup library:<br /> <hr style="width:98%;"/>
        <div id="popuplib_source" style="width:98%;height:400px;background-color:#ECECFF;overflow:scroll;padding-left:5px;border:1px solid #aaa">
        </div>
      </div>
    </div>
    <script type="text/javascript">
      $.ajax({ url: "/popuplib.js",
               success: function(response) {
                   $("#popuplib_source").html('<pre style="text-align:left;">'+response+'</pre>');
               }
             });
      var greetUser = function() {
        if ($("#stayOnPage").is(":checked")) {
          $.ajax({
              url: "/hello?size=short",
              cache: false,
              success: function(response) {
                $("#welcomePane").html(response);
              }
          });
        } else {
          window.location = "/hello";
        }
      };
      var extensions = <%= extensionParameters %>;
      var googleOpener = popupManager.createPopupOpener(
          { 'realm' : '<%= realm %>',
            'opEndpoint' : '<%= endpoint %>',
            'returnToUrl' : '<%= returnToUrl %>',
            'onCloseHandler' : greetUser,
            'shouldEncodeUrls' : true,
            'extensions' : extensions });
      document.getElementById("submit_button").onclick = function() {
        googleOpener.popup(450,500);
        return true;
      };
    </script>
  </body>
</html>
