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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.HavePBEFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.have_pbe
import util.ViewSpec

class HavePBEViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)
  private val formProvider     = new HavePBEFormProvider
  private def form             = formProvider.apply()
  private def formWithError    = form.bind(Map("value" -> ""))
  private val havePBEView      = instanceOf[have_pbe].apply(form)
  private val havePBEViewError = instanceOf[have_pbe].apply(formWithError)

  private lazy val havePBEDoc: Document          = Jsoup.parse(contentAsString(havePBEView))
  private lazy val havePBEDocWithError: Document = Jsoup.parse(contentAsString(havePBEViewError))

  "Have PBE page" should {

    "display correct title" in {
      havePBEDoc.title must startWith("Do you have a permanent business establishment in Northern Ireland?")
    }

    "display correct heading" in {
      havePBEDoc.body.getElementsByTag(
        "h1"
      ).text mustBe "Do you have a permanent business establishment in Northern Ireland?"
    }

    "display errors while empty form is submitted" in {
      havePBEDocWithError.body.getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Select your public business establishment preference"
    }

    "display yes no radio buttons" in {

      havePBEDoc.body.getElementById("value").attr("checked") mustBe empty
      havePBEDoc.body.getElementById("value-2").attr("checked") mustBe empty
    }

    "display hint" in {
      havePBEDoc.body.getElementById(
        "pbe-hint"
      ).text() mustBe "This is an address where you carry out customs activities related to moving goods between Northern Ireland and England, Scotland, Wales, the Isle of Man or the Channel Islands."
    }

  }

}
