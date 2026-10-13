//package ac.sfj.miniappcompetition.data.rule

//import ac.sfj.miniappcompetition.data.model.ParticipantType
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNull
//import org.junit.Test

//class ParticipantRulesTest {

//    @Test
//    fun normalizeMembers_trimsAndDropsEmptyFields() {
//        assertEquals(
//            listOf("Fajar Nugraha", "Gilang Ramadhan"),
//            ParticipantRules.normalizeMembers(listOf(" Fajar Nugraha ", "", "   ", "Gilang Ramadhan")),
//        )
//    }

//    @Test
//    fun membersForRequest_individualIsAlwaysNull() {
//        assertNull(ParticipantRules.membersForRequest(ParticipantType.INDIVIDUAL, listOf("Fajar")))
//        assertNull(ParticipantRules.membersForRequest(ParticipantType.INDIVIDUAL, emptyList()))
//    }

//    @Test
//    fun membersForRequest_teamSendsNormalizedList() {
//        assertEquals(listOf("Fajar"), ParticipantRules.membersForRequest(ParticipantType.TEAM, listOf(" Fajar ", " ")))
//        assertEquals(emptyList<String>(), ParticipantRules.membersForRequest(ParticipantType.TEAM, listOf("")))
//    }

//    @Test
//    fun validateName_trimsThenChecksBlankAndLength() {
//        assertEquals(NameError.BLANK, ParticipantRules.validateName("  "))
//        assertNull(ParticipantRules.validateName(" Tim Garuda "))
//        assertNull(ParticipantRules.validateName("a".repeat(255)))
//        assertEquals(NameError.TOO_LONG, ParticipantRules.validateName("a".repeat(256)))
//        assertEquals("Tim Garuda", ParticipantRules.normalizeName(" Tim Garuda "))
//    }
//}
