package com.myapp.controller

import com.myapp.domain.Relationship
import com.myapp.domain.User
import com.myapp.repository.RelationshipRepository
import com.myapp.repository.UserRepository
import com.myapp.service.SecurityContextService
import com.myapp.service.UserService
import com.myapp.service.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserRelationshipControllerTest extends BaseControllerTest {

    @Autowired
    UserRepository userRepository

    @Autowired
    RelationshipRepository relationshipRepository

    SecurityContextService securityContextService = Mock(SecurityContextService);

    @Override
    def controllers() {
        final UserService userService = new UserServiceImpl(userRepository, securityContextService)
        return new UserRelationshipController(userRepository, userService)
    }

    def "can list followings"() {
        given:
        User user1 = userRepository.save(new User(username: "akira@test.com", password: "secret", name: "akira"))
        User user2 = userRepository.save(new User(username: "satoru@test.com", password: "secret", name: "akira"))
        Relationship r1 = relationshipRepository.save(new Relationship(follower: user1, followed: user2))
        securityContextService.currentUser() >> userRepository.save(new User(username: "current@test.com", password: "secret", name: "akira"))

        when:
        def response = perform(get("/api/users/${user1.id}/followings"))

        then:
        response
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath('$[0].email', is("satoru@test.com")))
                .andExpect(jsonPath('$[0].isMyself', is(false)))
                .andExpect(jsonPath('$[0].userStats').exists())
                .andExpect(jsonPath('$[0].relationshipId', is(r1.id.intValue())))
    }

    def "can list followers"() {
        given:
        User user1 = userRepository.save(new User(username: "akira@test.com", password: "secret", name: "akira"))
        User user2 = userRepository.save(new User(username: "satoru@test.com", password: "secret", name: "akira"))
        Relationship r1 = relationshipRepository.save(new Relationship(follower: user2, followed: user1))
        securityContextService.currentUser() >> userRepository.save(new User(username: "current@test.com", password: "secret", name: "akira"))

        when:
        def response = perform(get("/api/users/${user1.id}/followers"))

        then:
        response
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath('$[0].email', is("satoru@test.com")))
                .andExpect(jsonPath('$[0].isMyself', is(false)))
                .andExpect(jsonPath('$[0].userStats').exists())
                .andExpect(jsonPath('$[0].relationshipId', is(r1.id.intValue())))
    }
}
