<#macro page old_text diff_text new_text style>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Diff</title>
        <style>
            <#include "style.css">
            <#include "${style}">
        </style>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6"
              crossorigin="anonymous">
    </head>
    <body>
    <header>
    </header>
    <div class="middle">
        <div class="main-field d-flex justify-content-center">
            <@writeTextCollumn "Original" old_text/>
            <@writeTextCollumn "Diff" diff_text/>
            <#if new_text?has_content>
                <@writeTextCollumn "New Text" new_text/>
            </#if>
        </div>
    </div>
    <footer>

    </footer>
    </body>
    </html>
</#macro>

<#macro writeStrings listOfStringsWithType>
    <#list listOfStringsWithType as stringWithType>
        <div class="${stringWithType.stringType} d-flex">${stringWithType.string}</div>
    </#list>
</#macro>

<#macro writeTextCollumn headerMessage listOfStringsWithType>
    <div class="old-field d-flex flex-column">
        <div class="field-header">${headerMessage}</div>
        <@writeStrings listOfStringsWithType/>
    </div>
</#macro>

