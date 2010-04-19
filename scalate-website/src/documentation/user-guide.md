# Scalate User Guide

* Table of contents
{:toc}

Scalate is a template engine based on the Scala language.

## Features

* Supports multiple template syntaxes
  * [SSP](ssp-reference.html) which is like [Velocity](http://velocity.apache.org/), JSP or Erb from Rails 
  * [Scaml](scaml-reference.html) which is a Scala dialect of [Haml](http://haml-lang.com/)
* Support for [layouts](#layouts) together with a powerful [console](console.html) 
* Works well with a number of [frameworks](frameworks.html)
* Can be used in any web application or used in a standalone application to template things like emails.

### Ssp (Scala Server Pages)

If you know [Velocity](http://velocity.apache.org/), JSP or Erb from Rails then hopefully the syntax of Ssp is familiar; only using Scala as the language of expressions and method invocations.

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<%@ var user: User %>
<p>Hi ${user.name},</p>
#for (i <- 1 to 3)
<p>${i}</p>
#end
<p>See, I can count!</p>
-----------------------------
xml: produces
-----------------------------
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

For full documentation of the Ssp syntax see the [Ssp Reference Guide](ssp-reference.html)


### Scaml (Scala Markup Language)

Scaml is a markup language
that's used to cleanly and simply describe the XHTML of any web document,
without the use of inline code.  It is Scala version of
[Haml](http://haml-lang.com/).
Scaml functions as a replacement
for inline page templating systems such as PHP, ERB, and ASP.
However, Scaml avoids the need for explicitly coding XHTML into the template,
because it is actually an abstract description of the XHTML,
with some code to generate dynamic content.

{pygmentize_and_compare::}
-----------------------------
haml: .scaml file
-----------------------------
-@ var user: User
%p Hi #{user.name},
- for(i <- 1 to 3)
  %p= i
%p See, I can count!
-----------------------------
xml: produces
-----------------------------
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

For full documentation of the Scaml syntax see the [Scaml Reference Guide](scaml-reference.html)

## Calling Scala functions

Its very simple to invoke any scala function inside Scalate. By default if the function you call returns NodeSeq then the output will already be properly XML encoded; so things output nicely without any possible cross scripting hacks etc.

For example the following function creates a hypertext link using Scala's XML support

{pygmentize:: scala}
object Cheese {
  def foo(productId: Int) = 
    <a href={"/products/" + productId} title="Product link">My Product</a>
}
{pygmentize}

This function can be invoked in your [Ssp](ssp-reference.html) code as

{pygmentize:: jsp}
<% import Cheese._  %>
${foo(123)}
{pygmentize}

If your template is in the same package as the <b>Cheese</b> class then the import is not required.

The [Scaml](scaml-reference.html) version is

{pygmentize:: haml}
- import Cheese._
= foo(123)    
{pygmentize}


### Accessing request state inside a Scala function

As you write snippet functions for use in your templates you might find yourself needing to access the current HttpServletRequest or HttpServletResponse.

There is a simple helper import you can use...

{pygmentize:: scala}
import org.fusesource.scalate.servlet.ServletRenderContext._

object MySnippets {
  def foo = {
    // thanks to the import I now have access to the renderContext
    // along with the standard servlet objects:
    // request, response, servletContext, servletConfig
    request.getParameter("foo") 
  }
}
{pygmentize}

This helps you keep your snippet functions nice and small.


### Passing a template block to a Scala function

To use the JSP concept of custom tags, you might want to pass a block of template to a function for further processing or transformation.

This can be done by just adding a parameter list of the form <b>(body: => Unit)</b> to your method. For example

{pygmentize:: scala}
import org.fusesource.scalate.RenderContext.capture

object Cheese {
  def foo(productId: Int)(body: => Unit) = 
    <a href={"/products/" + productId} title="Product link">capture(body)</a>
}
{pygmentize}

See how the body is captured using the <b>capture(body)</b> function. Now the text of the hypertext link can be specified as a block of template in [Ssp](ssp-reference.html)

{pygmentize:: jsp}
<%@ val id: Int = 123 %>
<% import Cheese._  %>

<%= foo(id) { %>
  product ${id}
<% } %>
{pygmentize}

This should generate something like

{pygmentize:: xml}
<a href="/products/123" title="Product link">product 123</a>
{pygmentize}


The [Scaml](scaml-reference.html) version is

{pygmentize:: haml}
-@ val id: Int = 123
- import Cheese._

= foo(id)  
  product #{id}  
{pygmentize}

Notice the [Scaml](scaml-reference.html) version is simpler, not requiring the open and close { } tokens as it uses indentation.


## Views

From within your Scala code or inside a template you often want to render an object or collection of objects. 
Scalate uses a convention over configuration mechanism so that you can render any object using a simple method call in your template. e.g. in SSP

{pygmentize:: jsp}
<%@ var it: User %>
<p>Something...</p>

<% view(it) %>

<p>... more stuff </p>
{pygmentize}

### View names

The view method takes a model object and an optional view name. The view name defaults to _"index"_ if you do not specify one. For exmaple you could have various views for an object such as _"index", "edit", "detail", etc._ Then you might want to show the edit view of an object via

{pygmentize:: jsp}
<%@ var it: User %>

<% view(it, "edit") %>
{pygmentize}

Scalate will then look for the template called _packageDirectory.ClassName.viewName.(ssp|scaml)_ and render that. For example in the sample web application to render an _org.fusesource.scalate.sample.Person_ object Scalate uses the [org/fusesource/scalate/sample/Person.index.ssp template](http://github.com/scalate/scalate/blob/master/scalate-sample/src/main/webapp/org/fusesource/scalate/sample/Person.index.ssp).

Notice that since the view is defined within the package of the model object, there is no need to import the _org.fusesource.scalate.sample.Person_, instead you can just refer to the model type directly as _Person_.

If a template is not found for the exact class name then the class and interface (trait) hierarchies are walked until one is found.

So for example you could provide a template for a generic trait you have - such as a template to render any [scala.Product](http://github.com/scalate/scalate/blob/master/scalate-sample/src/main/webapp/scala/Product.index.ssp) which will then render any case class; then you can customise the view on a class by class basis as required.

### The 'it' variable

By default we use the variable named _it_ to refer to the model parameter. This convention means that when working with JAXRS and Jersey's implicit views the model object is implicitly available to any templates using this naming convention.

### Collections

If you have a collection of objects you wish to view then you can use a simple helper method called *collection* which works like the *view* method described above.

{pygmentize:: jsp}
<%@ var it: List[Person] %>

<% collection(it) %>
{pygmentize}

As with the *view* method you can specify an optional view name if you won't want to use the _"index"_ default.

Also you can specify a separator to use between views of objects. The following example shows a horizontal line between views...

{pygmentize:: jsp}
<% val people = List(Person("James", "Strachan"), Person("Hiram", "Chirino")) %>
<% collection(people, separator = "<hr/>")  %>
{pygmentize}

Or without using named arguments

{pygmentize:: jsp}
<% collection(people, "index", "<hr/>")  %>
{pygmentize}

If a collection contains different types of objects then the correct view will be used for each element in the collection.

You can also supply a function for the separator if you want it to be dynamic

{pygmentize:: jsp}
<% var x = 1 %>
collection(people, separator = {x += 1; <h3>Person {x}</h3>})
{pygmentize}


## Render templates

It is common to want to refactor large templates into smaller reusable pieces. Its easy to render a template from inside another template with the *render* method as follows

{pygmentize:: jsp}
<% render("foo.ssp") %>
{pygmentize}

This will render a template called *foo.ssp* relative to the current template. You can use absolute names if you prefer

{pygmentize:: jsp}
<% render("/customers/contact.ssp") %>
{pygmentize}

You can also pass parameters into the template if it takes any

{pygmentize:: jsp}
<% render("/customers/contact.ssp", "customer" -> c, "title" -> "Customer") %>
{pygmentize}

When passing attributes you can use the Scala symbol notation for keys if you prefer...

{pygmentize:: jsp}
<% render("/customers/contact.ssp", 'customer -> c, 'title -> "Customer") %>
{pygmentize}

If you prefer you can pass in a body to the template using the *layout* method as described in [using explicit layouts inside a template](#explicit_layouts_inside_a_template).


## Layouts

Its quite common to want to style all pages in a similar way; such as adding a header and footer, a common navigation bar or including a common set of CSS stylesheets.

You can achieve this using the layout support in Scalate.

All you need to do is create a layout template in _/WEB-INF/layouts/default.ssp_ (or _/WEB-INF/layouts/default.scaml_ if you prefer). Here is a simple example layout which lays out the body and lets the title be customized on a per page basis.

{pygmentize:: jsp}
<%@ var body: String %>
<%@ var title: String = "Some Default Title" %>
<html>
<head>
  <title>${title}</title>
</head>
<body>
  <p>layout header goes here...</p>

  <%= body %>

  <p>layout footer goes here...</p>
</body>
</html>
{pygmentize}

Then all pages will be wrapped in this layout by default.

This means your templates don't need to include the whole html/head/body stuff, typically you'll just want the actual content to be displayed in the part of the layout you need. So a typical page might look like this...

{pygmentize:: jsp}
<h3>My Page</h3>
<p>This is some text</p>
{pygmentize}


### Changing the title or layout template

To set parameters on a layout or to change the layout template used, just output attribute values in your template.

{pygmentize:: jsp}
<% attributes("layout") = "/WEB-INF/layouts/custom.ssp" %>
<% attributes("title") = "This is the custom title" %>
<h3>Custom page</h3>
<p>This is some text</p>
{pygmentize}


### Disabling layouts

If you wish to disable the use of the layout on a template, just set the layout attribute to "" the empty string.

{pygmentize:: jsp}
<% attributes("layout") = "" %>
<html>
<body>
  <h1>No Layout</h1>
  <p>This page does not use the layout</p>
</body>
</html>
{pygmentize}

To see examples of layouts in use, try running the sample web application and looking at the layout related example pages.

### Explicit layouts inside a template

You may want to layout some content within part of your template explicitly rather than just applying a layout to an entire page.

For example you may want to create a layout as follows in file _foo.ssp_

{pygmentize:: jsp}
<%@ val body: String = "Bar" %>
<table>
 <tr>
   <th>Some Header</th>
 </tr>
 <tr>
   <td><%= body %></td>
 </tr>
</table>
{pygmentize}

Then we can invoke this template passing in the body as follows in [Ssp](ssp-reference.html)

{pygmentize:: jsp}
<% render("foo.ssp", "body" -> "Foo") %>
{pygmentize}

However if you want to pass in the body as a block of template you can use the *layout* method as follows

{pygmentize:: jsp}
<% layout("foo.ssp") {%>
Foo
<%}%>
{pygmentize}

Both will generate the same response.

Using the above mechanism via either the *render* or *layout* methods is quite like creating a JSP custom tag inside a .tag file if you come from a JSP background. 

The nice thing is there's really no difference technically between a regular template, a layout or a 'tag' template or a 'partial' (to use Rails terminology), they are all just templates which can have parameters which can be mandatory or optional.


## Capturing output

Sometimes you may wish to capture the result of rendering a block of template, assign it to a variable and then pass it as an argument to some method. For this the *capture* method can be used.

For example

{pygmentize:: jsp}
<% val foo = capture { %>
  hello there ${user.name} how are you?
<%}%>
...
${foo}
...
${foo}
{pygmentize}

We capture the block which generates a greeting, assign it to the _foo_ variable which we can then render or pass into methods etc.

The [Scaml](scaml-reference.html) version of this is a bit more concise

{pygmentize:: haml}
- var foo = capture
  hello there #{user.name} how are you?
    
...
= foo
...
= foo
{pygmentize}


## Scalate Samples

### Getting Started

The easiest way to get started is to try the [Getting Started Guide](getting-started.html)

### Running the Sample Web Application

The [source code](../source.html) comes with a sample web application called **scalate-sample** which includes a number of exmample templates you can play around with>

Scalate can be built either using Maven or SBT

#### Requirements

General Requirements:

* Java 5 runtime or Newer
* Scala 2.8 

Web Container Requirements:

* Servlet 2.5 container or Newer

#### Using Maven

Install [Maven](http://maven.apache.org/) version 2.0.9 or later. Then type

    mvn install
{: .syntax }

To run the sample web application

    cd scalate-sample
    mvn jetty:run
{: .syntax }

Then open [the sample home page](http://localhost:8080/)

#### Using SBT

You can also use [sbt](http://code.google.com/p/simple-build-tool/ "simple build tool") to build Scalate.

To setup your sbt environment and import the dependencies from the maven pom files type

    ./sbt
    update
{: .syntax }

Then to build the code type

    compile
{: .syntax }

to run the tests 

    test
{: .syntax }

For more information see the [sbt building instructions](http://scalate.fusesource.org/sbt.html)


## Using Scalate

You might want to refer to the [Frameworks Documentation](frameworks.html) to see if there is some specific instructions on using Scalate with your favourite web framework.

### Using Scalate with JAXRS/Jersey

Our recommendation is to start with [JOG](jog.html) (Jersey on Guice).

To get up to speed quickly with JOG try the [Getting Started Guide](getting-started.html) which uses the [WAR Overlay](war-overlay.html) to include the [Console](console.html) in your web application.

### Using Scalate as Servlets in your Web Application

* Add something like the following to your web.xml file to support Ssp and Scaml pages:

  {pygmentize:: {lang: xml, lines: true}}
  <servlet>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <servlet-class>org.fusesource.scalate.servlet.TemplateEngineServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <url-pattern>*.ssp</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <url-pattern>*.scaml</url-pattern>
  </servlet-mapping>
  {pygmentize}

*  Include the following JARs in your servlet's runtime environment (probably in WEB-INF/lib):
    * scala-compiler.jar
    * scala-library.jar
    * scalate-core.jar

You could add one or more of the above to your servlet container's server-wide configuration if you prefer

### Embedding Scalate in your Application or Framework

Scalate does not have any hard dependencies on a web framework or even HTTP.  It can be used as a standalone
rendering engine in your application.  For more information on how to embed in your application, please reference the 
[Scalate Embedding Guide](scalate-embedding-guide.html)

### Possible Gotchas

- Scalate works with expanded WARs - or servlet containers who's ClassLoader implements URLClassLoader or an AntClassLoader like thing. This is due to the Scala compiler requiring a full expended classloader path rather than taking an actual ClassLoader object.

- Assumes template source files are all UTF-8-encoded.

- Assumes template output is all UTF-8-encoded.

- No support for pre-compiled templates (e.g., via a custom Ant task).


## IDE plugins

Using an IDE plugin can make it much easier to view and edit Scalate templates.

### TextMate plugin

If you use [TextMate](http://macromates.com/) (which on OS X is a great text editor) you can install the [Scalate plugin](http://github.com/scalate/Scalate.tmbundle) as follows:

    cd ~/Library/Application\ Support/TextMate/Bundles/
    git clone git://github.com/scalate/Scalate.tmbundle.git
{: .syntax }

If you have not already done so you will also need a Scala plugin for TextMate which the [Ssp](ssp-reference.html) and [Scaml](scaml-reference.html) languages uses for the Scala code blocks.

We like the [version by Dean Wampler](http://github.com/deanwampler/Scala.tmbundle) though there's a few around github and one included in sbaz in the Scala distro too.

    cd ~/Library/Application\ Support/TextMate/Bundles/
    git clone git://github.com/deanwampler/Scala.tmbundle.git
{: .syntax }

When you restart TextMate you should now get syntax highlighting and more when you open up either a [Ssp](ssp-reference.html) or  [Scaml](scaml-reference.html) file. 

The current plugin does not highlight Scala expressions terribly well with the default Mac Classic colour scheme in TextMate. We found that it helps to add an extra colour to your scheme.

* open TextMate preferences via the TextMate menu or by hitting the apple key and ','
* select Fonts & Colors
* hit + to add a new entry
* type "Scaml expression" and pick a colour to use such as dark green (to differentiate from static markup text)
* in the **Scope Selector:** field enter **source.scala**
* you now should see Scala expressions in your Scaml file highlighted in green (so you can more easily tell the difference between static text and scala expressions)

### If you are an IDE hacker

We created Scalate specifically to be IDE friendly and we'd love to help create more and better IDE plugins (plus we [love contributions](../contributing.html)!). It should be easy to reuse any JSP / Erb / HAML IDE plugins but just swizzle them a little to use the Scala language instead - then get all the benefits of smart completion from Scala's static type system.

We've a [page on writing IDE plugins for Scalate](../creating-ide.html) which has more details on how an IDE plugin should ideally work for Scalate.




