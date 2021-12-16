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

package util

import akka.util.Timeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Lang.defaultLang
import play.api.i18n._
import play.api.mvc.Request
import play.api.test.CSRFTokenHelper
import play.twirl.api.Html

import scala.concurrent.duration._

trait ViewSpec extends PlaySpec with CSRFTest with Injector {

  private val messageApi: MessagesApi = instanceOf[MessagesApi]

  implicit val messages: Messages = MessagesImpl(defaultLang, messageApi)

  implicit val timeout: Timeout = 30.seconds
  val userId: String            = "someUserId"

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsHtml(doc: Document, cssSelector: String, expectedHtml: String): Assertion = {

    assertRenderedByCssSelector(doc, cssSelector)

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(doc.select(cssSelector).first().html().replace("\n", "") == expectedHtml)
  }

  def assertElementContainsText(doc: Document, cssSelector: String, expectedText: String): Assertion =
    assert(
      doc.select(cssSelector).get(0).text().contains(expectedText),
      s"\n\nElement located by $cssSelector does not have text $expectedText"
    )

  def assertRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")

  def assertPageH1Equals(doc: Document, expectedMessage: String): Assertion = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    headers.first.text.replaceAll("\u00a0", " ") mustBe expectedMessage.replaceAll("&nbsp;", " ")
  }

  def assertHasLink(doc: Document, cssSelector: String, text: String, url: String): Assertion = {
    assertRenderedByCssSelector(doc, cssSelector)
    assert(
      doc.select(cssSelector).attr("href") == url,
      "\n\nLink Element with selector " + cssSelector + " doesn't have the url" + url
    )
    assertElementContainsText(doc, cssSelector, text)

  }

  def title(heading: String)(implicit messages: Messages) =
    s"$heading - ${messages("service.name")} - ${messages("ecc.end-of-title")}"

}

import play.api.test.FakeRequest

trait CSRFTest {

  def withFakeCSRF[T](fakeRequest: FakeRequest[T]): Request[T] =
    CSRFTokenHelper.addCSRFToken(fakeRequest)

  val fakeRegisterRequest = FakeRequest("GET", "/")
}
