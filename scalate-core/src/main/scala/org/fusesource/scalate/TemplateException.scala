/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate

import scala.util.control.NoStackTrace
import scala.util.parsing.input.{ NoPosition, Position }
import support.CompilerError

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) {
    this (message, null)
  }
}

class ResourceNotFoundException(resource: String, root: String = "")
  extends TemplateException(
    "Could not load resource: [" + resource + 
    (if (root == "") "]" else "]; are you sure it's within [" + root + "]?"))

class InvalidSyntaxException(val brief: String, val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos) {
  var source:TemplateSource = _
  def template: String = if (source != null) source.uri else null
}

class CompilerException(msg:String, val errors:List[CompilerError]) extends TemplateException(msg)


class NoValueSetException(val attribute: String) extends TemplateException("The value for '" + attribute + "' was not set")

class NoFormParameterException(val parameter: String) extends TemplateException("The form parameter '" + parameter + "' was not set")

class NoSuchViewException(val model: AnyRef, val view: String) extends TemplateException("No '" + view +
        "' view template could be found for model object '" + model + "' of type: " + model.getClass.getCanonicalName)

class NoSuchFilterException(val filter: String) extends TemplateException("No '" + filter + "' filter available.")

class StaleCacheEntryException(source: TemplateSource)
  extends TemplateException("The compiled template for " + source + " needs to get recompiled") with NoStackTrace
