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

package models.cache

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.SubscriptionDisplayMongo
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionInfoVatId, XiSubscription}

import java.time.LocalDate

class SubscriptionDisplayModelSpec extends WordSpec with MustMatchers {

  "SubscriptionDisplayResponseDetail model" should {

    "correctly read only required information from json with one line address" in {

      val subscriptionDisplayJsonResponse = Json.parse("""{
                                                              |      "EORINo": "EN123456789012345",
                                                              |      "CDSFullName": "John Doe",
                                                              |      "CDSEstablishmentAddress": {
                                                              |        "streetAndNumber": "house no Line 1",
                                                              |        "city": "city name",
                                                              |        "postalCode": "SE28 1AA",
                                                              |        "countryCode": "ZZ"
                                                              |      },
                                                              |      "contactInformation": {
                                                              |        "personOfContact": "FirstName LastName",
                                                              |        "streetAndNumber": "line 1",
                                                              |        "city": "Newcastle",
                                                              |        "postalCode": "AA1 1AA",
                                                              |        "countryCode": "GB",
                                                              |        "telephoneNumber": "1234567890",
                                                              |        "emailAddress": "test@example.com"
                                                              |      },
                                                              |      "VATIDs": [
                                                              |        {
                                                              |          "countryCode": "GB",
                                                              |          "VATID": "999999"
                                                              |        },
                                                              |        {
                                                              |          "countryCode": "ES",
                                                              |          "VATID": "888888"
                                                              |        }
                                                              |      ],
                                                              |      "shortName": "Doe",
                                                              |      "dateOfEstablishment": "1963-04-01",
                                                              |      "XI_Subscription": {
                                                              |         "XI_EORINo":"XI8989989797",
                                                              |         "XI_VATNumber":"7978"
                                                              |      }
                                                              |}
                                                              | """.stripMargin)
      val xiSubscription: XiSubscription  = XiSubscription("XI8989989797", Some("7978"))
      val expectedModel = SubscriptionDisplayMongo(
        Some("EN123456789012345"),
        "John Doe",
        EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
        Some(
          ContactInformation(
            personOfContact = Some("FirstName LastName"),
            telephoneNumber = Some("1234567890"),
            emailAddress = Some("test@example.com"),
            streetAndNumber = Some("line 1"),
            city = Some("Newcastle"),
            postalCode = Some("AA1 1AA"),
            countryCode = Some("GB")
          )
        ),
        Some(
          List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))
        ),
        Some("Doe"),
        Some(LocalDate.of(1963, 4, 1)),
        Some(xiSubscription)
      )

      Json.toJson(expectedModel) mustBe subscriptionDisplayJsonResponse
    }

  }

}
