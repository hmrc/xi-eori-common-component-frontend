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

package models

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId,
  XiSubscription
}

import java.time.LocalDate

class SubscriptionDisplayResponseDetailSpec extends WordSpec with MustMatchers {

  "SubscriptionDisplayResponseDetail model" should {

    "correctly read only required information from json with one line address" in {

      val subscriptionDisplayJsonResponse = Json.parse("""{
                                                              |  "subscriptionDisplayResponse": {
                                                              |    "responseCommon": {
                                                              |      "status": "OK",
                                                              |      "processingDate": "2016-08-17T19:33:47Z",
                                                              |      "taxPayerID": "0100086619",
                                                              |      "returnParameters": [
                                                              |        {
                                                              |          "paramName": "ETMPFORMBUNDLENUMBER",
                                                              |          "paramValue": "9876543210"
                                                              |        },
                                                              |        {
                                                              |          "paramName": "POSITION",
                                                              |          "paramValue": "LINK"
                                                              |        }
                                                              |      ]
                                                              |    },
                                                              |    "responseDetail": {
                                                              |      "EORINo": "EN123456789012345",
                                                              |      "SAFEID": "XY0000100086619",
                                                              |      "CDSFullName": "John Doe",
                                                              |      "CDSEstablishmentAddress": {
                                                              |        "streetAndNumber": "house no Line 1",
                                                              |        "city": "city name",
                                                              |        "postalCode": "SE28 1AA",
                                                              |        "countryCode": "ZZ"
                                                              |      },
                                                              |      "establishmentInTheCustomsTerritoryOfTheUnion": "0",
                                                              |      "typeOfLegalEntity": "0001",
                                                              |      "contactInformation": {
                                                              |        "personOfContact": "John Doe",
                                                              |        "streetAndNumber": "Line 1",
                                                              |        "city": "city name",
                                                              |        "postalCode": "SE28 1AA",
                                                              |        "countryCode": "ZZ",
                                                              |        "telephoneNumber": "01632961234",
                                                              |        "faxNumber": "01632961235",
                                                              |        "emailAddress": "john.doe@example.com"
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
                                                              |      "thirdCountryUniqueIdentificationNumber": [
                                                              |        "321",
                                                              |        "222"
                                                              |      ],
                                                              |      "consentToDisclosureOfPersonalData": "1",
                                                              |      "shortName": "Doe",
                                                              |      "dateOfEstablishment": "1963-04-01",
                                                              |      "typeOfPerson": "1",
                                                              |      "principalEconomicActivity": "2000",
                                                              |      "XI_Subscription": {
                                                              |         "XI_EORINo":"XI8989989797",
                                                              |         "XI_VATNumber":"7978"
                                                              |      }
                                                              |    }
                                                              |  }
                                                              |}
                                                              | """.stripMargin)

      val result                         = SubscriptionDisplayResponseDetail.subscriptionDisplayReads.reads(subscriptionDisplayJsonResponse)
      val xiSubscription: XiSubscription = XiSubscription("XI8989989797", Some("7978"))
      val expectedModel = SubscriptionDisplayResponseDetail(
        Some("EN123456789012345"),
        "John Doe",
        EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
        Some(
          List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))
        ),
        Some("Doe"),
        Some(LocalDate.of(1963, 4, 1)),
        Some(xiSubscription)
      )

      result.get mustBe expectedModel
    }

  }
}
