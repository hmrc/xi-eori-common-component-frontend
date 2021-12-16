/*
 * Copyright 2021 HM Revenue & Customs
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

package views.behaviours

import org.jsoup.nodes.Document
import util.ViewSpec

trait ViewBehaviours extends ViewSpec {

  def normalPage(heading: String)(implicit document: Document): Unit =
    "behave like a normal page" when {

      "rendered" must {

        "display the correct browser title" in {
          assertEqualsHtml(document, "title", title(heading))
        }

        "display the correct page heading" in {
          assertPageH1Equals(document, heading)
        }

        "display Get help with this page link" in {
          assertRenderedById(document, "helpAndSupport")
        }

        "display Sign out link" in {
          assertRenderedByCssSelector(document, ".hmrc-sign-out-nav__link")
        }
      }
    }

  def pageWithBackLink(implicit document: Document): Unit =
    "behave like a page with a back link" must {

      "have a back link" in {
        assertRenderedById(document, "back-link")
      }
    }

  def pageWithSubmitButton(msg: String)(implicit document: Document): Unit =
    "behave like a page with a submit button" must {

      s"have a button with message '$msg'" in {
        assertEqualsHtml(document, ".govuk-button", msg)
      }
    }

}
