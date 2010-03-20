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
package org.fusesource.scalate.scaml

import org.fusesource.scalate.RenderContext

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ScamlOptions {

  // TODO lets enable this by default once SCALATE-50 is fixed
  var escape_html = true;

  object Format extends Enumeration {
    val xhtml, html4, html5 = Value
  }

  var format = Format.xhtml

  var autoclose = List(
    "meta",
    "img",
    "link",
    "script",
    "br",
    "hr")
}