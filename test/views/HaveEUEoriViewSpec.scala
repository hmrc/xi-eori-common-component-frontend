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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.HaveEUEoriFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.have_eu_eori
import util.ViewSpec

class HaveEUEoriViewSpec extends ViewSpec {

  private implicit val request    = withFakeCSRF(fakeRegisterRequest)
  private val formProvider        = new HaveEUEoriFormProvider
  private def form                = formProvider.apply()
  private def formWithError       = form.bind(Map("value" -> ""))
  private val haveEUEoriView      = instanceOf[have_eu_eori].apply(form)
  private val haveEUEoriViewError = instanceOf[have_eu_eori].apply(formWithError)

  private lazy val haveEUEoriDoc: Document          = Jsoup.parse(contentAsString(haveEUEoriView))
  private lazy val haveEUEoriDocWithError: Document = Jsoup.parse(contentAsString(haveEUEoriViewError))

  "You cannot use this service page for users of type standard org" should {

    "display correct title" in {
      haveEUEoriDoc.title must startWith(
        "Do you have an EU Economic Operator Registration and Identification (EORI) number?"
      )
    }

    "display correct heading" in {
      haveEUEoriDoc.body.getElementsByTag(
        "h1"
      ).text mustBe "Do you have an EU Economic Operator Registration and Identification (EORI) number?"
    }

    "display errors while empty form is submitted" in {
      haveEUEoriDocWithError.body.getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Tell us if you have an EU Economic Operator Registration and Identification (EORI) number"
    }

    "display yes no radio buttons" in {

      haveEUEoriDoc.body.getElementById("value").attr("checked") mustBe empty
      haveEUEoriDoc.body.getElementById("value-2").attr("checked") mustBe empty
    }

    "display hint" in {
      haveEUEoriDoc.body.getElementById("eu-eori-hint").text() mustBe "This can be issued by any country within the EU."
    }

    "display summary" in {
      haveEUEoriDoc.body.getElementById(
        "p3"
      ).text() mustBe "Austria, Belgium, Bulgaria, Croatia, Republic of Cyprus, Czech Republic, Denmark, Estonia, Finland, France, Germany, Greece, Hungary, Ireland, Italy, Latvia, Lithuania, Luxembourg, Malta, Netherlands, Poland, Portugal, Romania, Slovakia, Slovenia, Spain and Sweden."
    }

  }

}
