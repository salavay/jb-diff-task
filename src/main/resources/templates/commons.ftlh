<#macro page old_text diff_text new_text>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Diff</title>
        <style type="text/css">
            <#include "style.css">
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
            <div class="old-field">
                ${old_text}
            </div>
            <div class="diff-field d-flex flex-column">
                ${diff_text}
            </div>
            <div class="new-field">
                ${new_text}
            </div>
        </div>
    </div>
    <footer>

    </footer>
    </body>
    </html>
</#macro>